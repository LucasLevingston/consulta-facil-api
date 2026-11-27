package com.consultafacil.application.service.subscription;

import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.enums.SubscriptionPlan;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.port.out.subscription.SubscriptionRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetEffectivePlanServiceTest {

    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;

    @InjectMocks
    private GetEffectivePlanService service;

    @Test
    void noSubscription_returnsFree() {
        when(subscriptionRepository.findByUserId("u1")).thenReturn(Optional.empty());
        assertEquals(SubscriptionPlan.FREE, service.execute("u1"));
    }

    @Test
    void activeTrial_returnsProfessional() {
        Subscription sub = Subscription.builder()
                .plan(SubscriptionPlan.FREE)
                .status(SubscriptionStatus.ACTIVE)
                .trialEndsAt(LocalDateTime.now().plusDays(10))
                .build();
        when(subscriptionRepository.findByUserId("u1")).thenReturn(Optional.of(sub));
        assertEquals(SubscriptionPlan.PROFESSIONAL, service.execute("u1"));
    }

    @Test
    void expiredTrial_returnsFree() {
        Subscription sub = Subscription.builder()
                .plan(SubscriptionPlan.FREE)
                .status(SubscriptionStatus.EXPIRED)
                .trialEndsAt(LocalDateTime.now().minusDays(1))
                .build();
        when(subscriptionRepository.findByUserId("u1")).thenReturn(Optional.of(sub));
        assertEquals(SubscriptionPlan.FREE, service.execute("u1"));
    }

    @Test
    void activeSubscription_returnsPlan() {
        Subscription sub = Subscription.builder()
                .plan(SubscriptionPlan.PROFESSIONAL)
                .status(SubscriptionStatus.ACTIVE)
                .build();
        when(subscriptionRepository.findByUserId("u1")).thenReturn(Optional.of(sub));
        assertEquals(SubscriptionPlan.PROFESSIONAL, service.execute("u1"));
    }

    @Test
    void clinicPlan_active_returnsClinic() {
        Subscription sub = Subscription.builder()
                .plan(SubscriptionPlan.CLINIC)
                .status(SubscriptionStatus.ACTIVE)
                .build();
        when(subscriptionRepository.findByUserId("u1")).thenReturn(Optional.of(sub));
        assertEquals(SubscriptionPlan.CLINIC, service.execute("u1"));
    }
}
