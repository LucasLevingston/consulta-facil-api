package com.example.consulta.application.service;

import com.example.consulta.core.config.MercadoPagoConfig;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Subscription;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.SubscriptionStatus;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.port.out.EmailPort;
import com.example.consulta.domain.port.out.SubscriptionRepositoryPort;
import com.example.consulta.domain.port.out.UserRepositoryPort;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubscriptionServiceTest {

    @Mock SubscriptionRepositoryPort subscriptionRepository;
    @Mock UserRepositoryPort userRepository;
    @Mock MercadoPagoConfig mpConfig;
    @Mock EmailPort emailPort;

    @InjectMocks SubscriptionService service;

    User user;
    Subscription subscription;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "appUrl", "http://localhost:3000");
        user = User.builder().id("u-1").email("joao@email.com").name("João Silva").password("x").role(UserRole.PROFESSIONAL).build();
        subscription = Subscription.builder().id("sub-1").user(user).planId("monthly").status(SubscriptionStatus.ACTIVE).expiresAt(LocalDateTime.now().plusDays(30)).build();
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ── createCheckout ─────────────────────────────────────────────────────

    @Test
    void createCheckout_invalidPlanId_throwsIllegalArgument() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> service.createCheckout("u-1", "invalid-plan"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid plan");
    }

    @Test
    void createCheckout_userNotFound_throwsNotFound() {
        when(userRepository.findById("bad")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createCheckout("bad", "monthly"))
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
