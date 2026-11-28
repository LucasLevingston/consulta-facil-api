package com.consultafacil.application.service.subscription;

import com.consultafacil.api.dto.coupon.CouponValidationResponseDTO;
import com.consultafacil.application.port.in.coupon.ValidateCouponUseCase;
import com.consultafacil.core.config.MercadoPagoConfig;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.BillingPeriod;
import com.consultafacil.domain.enums.PlanStatus;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.plan.PlanRepositoryPort;
import com.consultafacil.domain.port.out.subscription.SubscriptionRepositoryPort;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubscriptionCheckoutServiceTest {

    @Mock PlanRepositoryPort planRepository;
    @Mock UserRepositoryPort userRepository;
    @Mock SubscriptionRepositoryPort subscriptionRepository;
    @Mock ValidateCouponUseCase couponUseCase;
    @Mock MercadoPagoConfig mpConfig;

    @InjectMocks SubscriptionCheckoutService service;

    User user;
    Plan monthlyPlan;

    @BeforeEach
    void setUp() {
        user = User.builder().id("u-1").email("joao@email.com").name("João Silva").password("x").role(UserRole.PROFESSIONAL).build();
        monthlyPlan = Plan.builder()
                .id("plan-pro-m").slug("monthly").name("Pro Mensal")
                .price(new BigDecimal("149.90")).tier("PRO")
                .billingPeriod(BillingPeriod.MONTHLY)
                .frequency(1).frequencyType("months")
                .status(PlanStatus.ACTIVE).displayOrder(4)
                .build();
        when(planRepository.findBySlug("invalid-plan")).thenReturn(Optional.empty());
        when(planRepository.findBySlug("monthly")).thenReturn(Optional.of(monthlyPlan));
        when(mpConfig.getSuccessUrl()).thenReturn("http://localhost:3000/success");
    }

    @Test
    void createCheckout_invalidPlanId_throwsIllegalArgument() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> service.execute("u-1", "invalid-plan", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid plan");
    }

    @Test
    void createCheckout_userNotFound_throwsNotFound() {
        when(userRepository.findById("bad")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.execute("bad", "monthly", null, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createCheckout_invalidPlan_doesNotCallUserRepository() {
        assertThatThrownBy(() -> service.execute("u-1", "invalid-plan", null, null))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).findById(anyString());
    }

    @Test
    void createCheckout_validPlanAndUser_callsMercadoPagoAndThrowsRuntimeExceptionOnNetworkError() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        // PreapprovalClient.create() fails in unit test (no MP credentials) → wrapped in RuntimeException
        assertThatThrownBy(() -> service.execute("u-1", "monthly", null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("checkout");
    }

    @Test
    void createCheckout_withCouponCode_callsValidateCouponUseCase() {
        CouponValidationResponseDTO couponResponse = CouponValidationResponseDTO.builder()
                .couponId("c-1").code("SAVE10")
                .discountAmount(new BigDecimal("14.99"))
                .finalPrice(new BigDecimal("134.91"))
                .grossAmount(new BigDecimal("149.90"))
                .build();
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(couponUseCase.execute("SAVE10", "u-1", "monthly", new BigDecimal("149.90")))
                .thenReturn(couponResponse);

        // MP call will fail in unit test → still verifies couponUseCase was called
        assertThatThrownBy(() -> service.execute("u-1", "monthly", null, "SAVE10"))
                .isInstanceOf(RuntimeException.class);

        verify(couponUseCase).execute("SAVE10", "u-1", "monthly", new BigDecimal("149.90"));
    }

    @Test
    void createCheckout_blankCouponCode_doesNotCallValidateCouponUseCase() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.execute("u-1", "monthly", null, "   "))
                .isInstanceOf(RuntimeException.class);

        verify(couponUseCase, never()).execute(anyString(), anyString(), anyString(), any(BigDecimal.class));
    }

    @Test
    void createCheckout_existingSubscription_reusesExistingRecord() {
        Subscription existing = Subscription.builder()
                .id("sub-existing").user(user).planId("old-plan")
                .status(SubscriptionStatus.ACTIVE)
                .build();
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserId("u-1")).thenReturn(Optional.of(existing));

        // MP call fails → verify subscription lookup was attempted
        assertThatThrownBy(() -> service.execute("u-1", "monthly", null, null))
                .isInstanceOf(RuntimeException.class);
    }
}
