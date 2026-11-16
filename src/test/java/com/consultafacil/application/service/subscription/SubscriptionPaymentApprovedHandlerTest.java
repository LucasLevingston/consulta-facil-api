package com.consultafacil.application.service.subscription;

import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.BillingPeriod;
import com.consultafacil.domain.enums.PlanStatus;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.plan.PlanRepositoryPort;
import com.consultafacil.domain.port.out.subscription.SubscriptionPaymentRepositoryPort;
import com.consultafacil.domain.port.out.subscription.SubscriptionRepositoryPort;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubscriptionPaymentApprovedHandlerTest {

    @Mock SubscriptionRepositoryPort subscriptionRepository;
    @Mock SubscriptionPaymentRepositoryPort subscriptionPaymentRepository;
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

    /** Runs the handler with MercadoPago returning a payment with the given status and reference. */
    private MockedConstruction<PaymentClient> mercadoPagoReturns(String status, String externalReference) {
        Payment payment = mock(Payment.class);
        when(payment.getStatus()).thenReturn(status);
        when(payment.getExternalReference()).thenReturn(externalReference);
        return mockConstruction(PaymentClient.class,
                (client, ctx) -> when(client.get(anyLong())).thenReturn(payment));
    }

    @Test
    void approvedPayment_activatesPendingSubscriptionAndRecordsIt() {
        subscription.setStatus(SubscriptionStatus.PENDING);
        subscription.setExpiresAt(null);
        when(subscriptionRepository.findByUserId("u-1")).thenReturn(Optional.of(subscription));

        try (var mp = mercadoPagoReturns("approved", "u-1|monthly")) {
            handler.execute("1001");
        }

        verify(subscriptionRepository).save(subscription);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(subscription.getMpPaymentId()).isEqualTo("1001");
        assertThat(subscription.getExpiresAt()).isAfter(LocalDateTime.now().plusDays(25));
        verify(paymentRecorder).recordPayment("sub-1", "1001", new BigDecimal("149.90"), "CREDIT_CARD");
        verify(renewalNotifier).sendRenewalEmail(eq(user), eq(monthlyPlan), any());
    }

    @Test
    void approvedPayment_extendsAnActiveSubscriptionFromItsCurrentExpiry() {
        LocalDateTime currentExpiry = LocalDateTime.now().plusDays(15);
        subscription.setExpiresAt(currentExpiry);
        when(subscriptionRepository.findByUserId("u-1")).thenReturn(Optional.of(subscription));

        try (var mp = mercadoPagoReturns("approved", "u-1|monthly")) {
            handler.execute("1001");
        }

        assertThat(subscription.getExpiresAt()).isAfter(currentExpiry.plusDays(25));
    }

    @Test
    void approvedPayment_createsSubscriptionForUserWithoutOne() {
        when(subscriptionRepository.findByUserId("u-1")).thenReturn(Optional.empty());
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));

        try (var mp = mercadoPagoReturns("approved", "u-1|monthly")) {
            handler.execute("1001");
        }

        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void alreadyAppliedPayment_isIgnoredWithoutCallingMercadoPago() {
        when(subscriptionPaymentRepository.existsByMpPaymentId("1001")).thenReturn(true);

        try (var mp = mercadoPagoReturns("approved", "u-1|monthly")) {
            handler.execute("1001");
            assertThat(mp.constructed()).isEmpty();
        }

        verify(subscriptionRepository, never()).save(any());
        verifyNoInteractions(paymentRecorder, renewalNotifier);
    }

    @Test
    void paymentNotApproved_changesNothing() {
        try (var mp = mercadoPagoReturns("rejected", "u-1|monthly")) {
            handler.execute("1001");
        }

        verify(subscriptionRepository, never()).save(any());
        verifyNoInteractions(paymentRecorder);
    }

    @Test
    void paymentWithForeignReference_changesNothing() {
        try (var mp = mercadoPagoReturns("approved", "appointment|appt-1")) {
            handler.execute("1001");
        }

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void paymentWithoutReference_changesNothing() {
        try (var mp = mercadoPagoReturns("approved", null)) {
            handler.execute("1001");
        }

        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void unknownPlan_changesNothing() {
        when(subscriptionRepository.findByUserId("u-1")).thenReturn(Optional.of(subscription));

        try (var mp = mercadoPagoReturns("approved", "u-1|unknown-plan")) {
            handler.execute("1001");
        }

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void mercadoPagoFailure_isSwallowed() {
        try (var mp = mockConstruction(PaymentClient.class,
                (client, ctx) -> when(client.get(anyLong())).thenThrow(new RuntimeException("timeout")))) {
            assertThatCode(() -> handler.execute("1001")).doesNotThrowAnyException();
        }

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void nonNumericPaymentId_isIgnored() {
        assertThatCode(() -> handler.execute("not-a-number")).doesNotThrowAnyException();

        verify(subscriptionRepository, never()).save(any());
    }
}
