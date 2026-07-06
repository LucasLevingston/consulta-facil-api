package com.consultafacil.application.service;

import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.BillingPeriod;
import com.consultafacil.domain.enums.PlanStatus;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.PlanRepositoryPort;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubscriptionPaymentApprovedHandlerTest {

    @Mock SubscriptionRepositoryPort subscriptionRepository;
    @Mock UserRepositoryPort userRepository;
    @Mock PlanRepositoryPort planRepository;
    @Mock SubscriptionPaymentRecorder paymentRecorder;
    @Mock SubscriptionRenewalNotifier renewalNotifier;

    @InjectMocks SubscriptionPaymentApprovedHandler handler;

    User user;
    Subscription subscription;
    Plan monthlyPlan;

    @BeforeEach
    void setUp() {
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
        when(planRepository.findBySlug("monthly")).thenReturn(Optional.of(monthlyPlan));
        when(planRepository.findBySlug("unknown-plan")).thenReturn(Optional.empty());
    }

    @Test
    void handle_validRef_activatesAndExtendsSubscription() {
        subscription.setStatus(SubscriptionStatus.PENDING);
        subscription.setExpiresAt(null);
        when(subscriptionRepository.findByUserId("u-1")).thenReturn(Optional.of(subscription));

        handler.handle("pay-1", "u-1|monthly");

        verify(subscriptionRepository).save(subscription);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(subscription.getExpiresAt()).isAfter(LocalDateTime.now().plusDays(25));
    }

    @Test
    void handle_activeSubscription_extendsFromCurrentExpiry() {
        LocalDateTime currentExpiry = LocalDateTime.now().plusDays(15);
        subscription.setExpiresAt(currentExpiry);
        when(subscriptionRepository.findByUserId("u-1")).thenReturn(Optional.of(subscription));

        handler.handle("pay-1", "u-1|monthly");

        assertThat(subscription.getExpiresAt()).isAfter(currentExpiry.plusDays(25));
    }

    @Test
    void handle_invalidRef_doesNothing() {
        handler.handle("pay-1", "invalid-reference");
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void handle_nullRef_fetchesFromMercadoPago() {
        // With null externalReference, falls through to PaymentClient — just verify no NPE
        // (PaymentClient will fail in unit context, which is caught and logged)
        handler.handle("12345", null);
    }

    @Test
    void handle_unknownPlanId_doesNothing() {
        when(subscriptionRepository.findByUserId("u-1")).thenReturn(Optional.of(subscription));
        handler.handle("pay-1", "u-1|unknown-plan");
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void handle_sendsRenewalEmail() {
        subscription.setExpiresAt(null);
        when(subscriptionRepository.findByUserId("u-1")).thenReturn(Optional.of(subscription));

        handler.handle("pay-1", "u-1|monthly");

        verify(renewalNotifier).sendRenewalEmail(eq(user), eq(monthlyPlan), any());
    }
}
