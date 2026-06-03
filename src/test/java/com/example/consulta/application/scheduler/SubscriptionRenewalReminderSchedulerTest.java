package com.example.consulta.application.scheduler;

import com.example.consulta.domain.entity.Subscription;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.SubscriptionStatus;
import com.example.consulta.domain.port.out.EmailPort;
import com.example.consulta.domain.port.out.SubscriptionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionRenewalReminderSchedulerTest {

    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;

    @Mock
    private EmailPort emailPort;

    @InjectMocks
    private SubscriptionRenewalReminderScheduler scheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "appUrl", "http://localhost:3000");
    }

    @Test
    void sendRenewalReminders_sendsEmailForExpiringSoon() {
        User user = User.builder().id("u1").email("pro@test.com").name("Carlos").build();
        Subscription sub = Subscription.builder()
                .id("sub-1")
                .user(user)
                .planId("clinic-monthly")
                .status(SubscriptionStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(subscriptionRepository.findActiveExpiringBetween(any(), any())).thenReturn(List.of(sub));

        scheduler.sendRenewalReminders();

        verify(emailPort).sendSubscriptionRenewalReminder(
                eq("pro@test.com"), eq("Carlos"), eq("Plano Clínica Mensal"), eq(7),
                contains("/planos"));
    }

    @Test
    void sendRenewalReminders_noneExpiringSoon_doesNothing() {
        when(subscriptionRepository.findActiveExpiringBetween(any(), any())).thenReturn(List.of());

        scheduler.sendRenewalReminders();

        verifyNoInteractions(emailPort);
    }

    @Test
    void sendRenewalReminders_emailFailure_continuesOtherSubscriptions() {
        User u1 = User.builder().id("u1").email("ok@test.com").name("Ana").build();
        User u2 = User.builder().id("u2").email("fail@test.com").name("Pedro").build();
        Subscription s1 = Subscription.builder().id("s1").user(u1).planId("monthly")
                .status(SubscriptionStatus.ACTIVE).expiresAt(LocalDateTime.now().plusDays(7)).build();
        Subscription s2 = Subscription.builder().id("s2").user(u2).planId("yearly")
                .status(SubscriptionStatus.ACTIVE).expiresAt(LocalDateTime.now().plusDays(7)).build();

        when(subscriptionRepository.findActiveExpiringBetween(any(), any())).thenReturn(List.of(s1, s2));
        doThrow(new RuntimeException("SES error")).when(emailPort)
                .sendSubscriptionRenewalReminder(eq("ok@test.com"), any(), any(), anyInt(), any());

        scheduler.sendRenewalReminders();

        verify(emailPort, times(2)).sendSubscriptionRenewalReminder(any(), any(), any(), anyInt(), any());
    }

    @Test
    void sendRenewalReminders_unknownPlanId_fallsBackToPlanIdAsLabel() {
        User user = User.builder().id("u1").email("x@test.com").name("Xena").build();
        Subscription sub = Subscription.builder()
                .id("sub-x")
                .user(user)
                .planId("enterprise")
                .status(SubscriptionStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(subscriptionRepository.findActiveExpiringBetween(any(), any())).thenReturn(List.of(sub));

        scheduler.sendRenewalReminders();

        verify(emailPort).sendSubscriptionRenewalReminder(
                eq("x@test.com"), eq("Xena"), eq("enterprise"), eq(7), any());
    }
}
