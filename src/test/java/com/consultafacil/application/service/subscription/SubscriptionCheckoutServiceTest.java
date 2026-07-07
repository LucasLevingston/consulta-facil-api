package com.consultafacil.application.service.subscription;

import com.consultafacil.application.port.in.ValidateCouponUseCase;
import com.consultafacil.core.config.MercadoPagoConfig;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.BillingPeriod;
import com.consultafacil.domain.enums.PlanStatus;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
                .price(new java.math.BigDecimal("149.90")).tier("PRO")
                .billingPeriod(BillingPeriod.MONTHLY)
                .frequency(1).frequencyType("months")
                .status(PlanStatus.ACTIVE).displayOrder(4)
                .build();
        when(planRepository.findBySlug("invalid-plan")).thenReturn(Optional.empty());
        when(planRepository.findBySlug("monthly")).thenReturn(Optional.of(monthlyPlan));
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
}
