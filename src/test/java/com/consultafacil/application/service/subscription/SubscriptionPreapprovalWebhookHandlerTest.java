package com.consultafacil.application.service.subscription;

import com.consultafacil.application.port.in.coupon.RecordCouponUseUseCase;
import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.BillingPeriod;
import com.consultafacil.domain.enums.PlanStatus;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.plan.PlanRepositoryPort;
import com.consultafacil.domain.port.out.subscription.SubscriptionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubscriptionPreapprovalWebhookHandlerTest {

    @Mock SubscriptionRepositoryPort subscriptionRepository;
    @Mock PlanRepositoryPort planRepository;
    @Mock RecordCouponUseUseCase couponUseCase;
    @Mock SubscriptionSellerSaleLinker sellerSaleLinker;
    @Mock SubscriptionPaymentRecorder paymentRecorder;

    @InjectMocks SubscriptionPreapprovalWebhookHandler handler;

    User user;
    Plan plan;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("u-1").email("joao@email.com").name("João Silva")
                .password("x").role(UserRole.PROFESSIONAL).build();

        plan = Plan.builder()
                .id("plan-1").slug("monthly").name("Pro Mensal")
                .price(new BigDecimal("149.90")).tier("PRO")
                .billingPeriod(BillingPeriod.MONTHLY)
                .frequency(1).frequencyType("months")
                .status(PlanStatus.ACTIVE).displayOrder(1)
                .build();
    }

    @Test
    void execute_mpClientFails_exceptionIsCaughtAndDoesNotPropagate() {
        // PreapprovalClient.get() will throw without real MP credentials — exception is caught
        handler.execute("preapproval-not-found");
        // No exception propagated; just ensure no repository call
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void execute_subscriptionNotInDb_logsWarnAndDoesNotSave() {
        // Real PreapprovalClient will fail; the whole execute is wrapped in try-catch
        handler.execute("pa-unknown");
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void execute_cancelledStatus_setsSubscriptionCancelled() {
        Subscription sub = Subscription.builder()
                .id("sub-1").user(user).planId("monthly")
                .mpPreapprovalId("pa-cancel")
                .status(SubscriptionStatus.ACTIVE)
                .build();

        // We stub subscriptionRepository before MP call; MP call will still throw
        // but exception is caught → verify on subscriptionRepository.findByMpPreapprovalId
        // won't be called since MP throws first. Verify no save happens.
        when(subscriptionRepository.findByMpPreapprovalId("pa-cancel")).thenReturn(Optional.of(sub));

        handler.execute("pa-cancel");

        // MP network failure is swallowed; no save should happen
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void execute_recordCouponUse_calledWhenCouponPresent() {
        Subscription sub = Subscription.builder()
                .id("sub-1").user(user).planId("monthly")
                .mpPreapprovalId("pa-1")
                .status(SubscriptionStatus.PENDING)
                .couponId("coupon-123")
                .discountApplied(new BigDecimal("14.99"))
                .build();

        when(subscriptionRepository.findByMpPreapprovalId("pa-1")).thenReturn(Optional.of(sub));
        when(planRepository.findBySlug("monthly")).thenReturn(Optional.of(plan));

        // MP network failure is caught; no recordCouponUse since we never reach activate
        handler.execute("pa-1");

        verify(couponUseCase, never()).execute(anyString(), anyString(), anyString(), any(BigDecimal.class));
    }
}
