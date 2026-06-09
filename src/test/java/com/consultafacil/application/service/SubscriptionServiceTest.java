package com.consultafacil.application.service;

import com.consultafacil.api.dto.tax.TaxBreakdown;
import com.consultafacil.core.config.MercadoPagoConfig;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.BillingPeriod;
import com.consultafacil.domain.enums.PlanStatus;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.EmailPort;
import com.consultafacil.domain.port.out.PlanRepositoryPort;
import com.consultafacil.domain.port.out.SubscriptionPaymentRepositoryPort;
import com.consultafacil.domain.port.out.SubscriptionRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubscriptionServiceTest {

    @Mock SubscriptionRepositoryPort subscriptionRepository;
    @Mock UserRepositoryPort userRepository;
    @Mock PlanRepositoryPort planRepository;
    @Mock SubscriptionPaymentRepositoryPort subscriptionPaymentRepository;
    @Mock TaxCalculationService taxCalculationService;
    @Mock MercadoPagoConfig mpConfig;
    @Mock EmailPort emailPort;

    @InjectMocks SubscriptionService service;

    User user;
    Subscription subscription;
    Plan monthlyPlan;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "appUrl", "http://localhost:3000");
        user = User.builder().id("u-1").email("joao@email.com").name("João Silva").password("x").role(UserRole.PROFESSIONAL).build();
        subscription = Subscription.builder().id("sub-1").user(user).planId("monthly").status(SubscriptionStatus.ACTIVE).expiresAt(LocalDateTime.now().plusDays(30)).build();

        monthlyPlan = Plan.builder()
                .id("plan-pro-m").slug("monthly").name("Pro Mensal")
                .price(new BigDecimal("149.90")).tier("PRO")
                .billingPeriod(BillingPeriod.MONTHLY)
                .frequency(1).frequencyType("months")
                .status(PlanStatus.ACTIVE).displayOrder(4)
                .build();

        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(subscriptionPaymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(planRepository.findBySlug("monthly")).thenReturn(Optional.of(monthlyPlan));
        when(planRepository.findBySlug("invalid-plan")).thenReturn(Optional.empty());
        when(planRepository.findBySlug("unknown-plan")).thenReturn(Optional.empty());

        TaxBreakdown dummyTax = new TaxBreakdown(
                new java.math.BigDecimal("149.90"),
                new java.math.BigDecimal("7.47"),
                new java.math.BigDecimal("8.99"),
                java.math.BigDecimal.ZERO,
                new java.math.BigDecimal("133.44"),
                new java.math.BigDecimal("6.0"),
                "SIMPLES_NACIONAL", "CREDIT_CARD");
        when(taxCalculationService.calculate(any(), any())).thenReturn(dummyTax);
        when(taxCalculationService.buildSnapshot(any())).thenReturn("{}");
    }

    // ── createCheckout ─────────────────────────────────────────────────────

    @Test
    void createCheckout_invalidPlanId_throwsIllegalArgument() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> service.createCheckout("u-1", "invalid-plan", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid plan");
    }

    @Test
    void createCheckout_userNotFound_throwsNotFound() {
        when(userRepository.findById("bad")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createCheckout("bad", "monthly", null, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getMySubscription ─────────────────────────────────────────────────

    @Test
    void getMySubscription_existingSubscription_returnsDTO() {
        when(subscriptionRepository.findByUserId("u-1")).thenReturn(Optional.of(subscription));
        var result = service.getMySubscription("u-1");
        assertThat(result).isPresent();
        assertThat(result.get().getPlanId()).isEqualTo("monthly");
        assertThat(result.get().getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void getMySubscription_noSubscription_returnsEmpty() {
        when(subscriptionRepository.findByUserId("u-1")).thenReturn(Optional.empty());
        var result = service.getMySubscription("u-1");
        assertThat(result).isEmpty();
    }

    // ── handlePaymentApproved ─────────────────────────────────────────────

    @Test
    void handlePaymentApproved_validRef_activatesAndExtendsSubscription() {
        subscription.setStatus(SubscriptionStatus.PENDING);
        subscription.setExpiresAt(null);
        when(subscriptionRepository.findByUserId("u-1")).thenReturn(Optional.of(subscription));

        service.handlePaymentApproved("pay-1", "u-1|monthly");

        verify(subscriptionRepository).save(subscription);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(subscription.getExpiresAt()).isAfter(LocalDateTime.now().plusDays(25));
    }

    @Test
    void handlePaymentApproved_activeSubscription_extendsFromCurrentExpiry() {
        LocalDateTime currentExpiry = LocalDateTime.now().plusDays(15);
        subscription.setExpiresAt(currentExpiry);
        when(subscriptionRepository.findByUserId("u-1")).thenReturn(Optional.of(subscription));

        service.handlePaymentApproved("pay-1", "u-1|monthly");

        assertThat(subscription.getExpiresAt()).isAfter(currentExpiry.plusDays(25));
    }

    @Test
    void handlePaymentApproved_invalidRef_doesNothing() {
        service.handlePaymentApproved("pay-1", "invalid-reference");
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void handlePaymentApproved_nullRef_fetchesFromMercadoPago() {
        // With null externalReference, falls through to PaymentClient — just verify no NPE
        // (PaymentClient will fail in unit context, which is caught and logged)
        service.handlePaymentApproved("12345", null);
    }

    @Test
    void handlePaymentApproved_unknownPlanId_doesNothing() {
        when(subscriptionRepository.findByUserId("u-1")).thenReturn(Optional.of(subscription));
        service.handlePaymentApproved("pay-1", "u-1|unknown-plan");
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void handlePaymentApproved_sendsRenewalEmail() {
        subscription.setExpiresAt(null);
        when(subscriptionRepository.findByUserId("u-1")).thenReturn(Optional.of(subscription));
        when(mpConfig.getSuccessUrl()).thenReturn("http://localhost:3000/billing/success");

        service.handlePaymentApproved("pay-1", "u-1|monthly");

        verify(emailPort).sendSubscriptionRenewed(
                eq("joao@email.com"), eq("João Silva"), any(), any(), any());
    }

    // ── handlePreapprovalWebhook ──────────────────────────────────────────

    @Test
    void handlePreapprovalWebhook_notFoundInDB_doesNotThrow() {
        // PreapprovalClient will fail in unit context but exception is caught
        service.handlePreapprovalWebhook("preapproval-1");
    }
}
