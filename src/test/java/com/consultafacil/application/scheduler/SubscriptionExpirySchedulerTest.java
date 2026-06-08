package com.consultafacil.application.scheduler;

import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.port.out.EmailPort;
import com.consultafacil.domain.port.out.SubscriptionRepositoryPort;
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
class SubscriptionExpirySchedulerTest {

    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;

    @Mock
    private EmailPort emailPort;

    @InjectMocks
    private SubscriptionExpiryScheduler scheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "appUrl", "http://localhost:3000");
    }

    @Test
    void expireSubscriptions_marksExpiredAndSendsEmail() {
        User user = User.builder().id("u1").email("user@test.com").name("Maria").build();
        Subscription sub = Subscription.builder()
                .id("sub-1")
                .user(user)
                .planId("monthly")
                .status(SubscriptionStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();

        when(subscriptionRepository.findActiveExpiredBefore(any())).thenReturn(List.of(sub));
        when(subscriptionRepository.save(any())).thenReturn(sub);

        scheduler.expireSubscriptions();

        verify(subscriptionRepository).save(argThat(s -> s.getStatus() == SubscriptionStatus.EXPIRED));
        verify(emailPort).sendSubscriptionExpired(
                eq("user@test.com"), eq("Maria"), eq("Plano Pro Mensal"),
                contains("/planos"));
    }

    @Test
    void expireSubscriptions_noExpired_doesNothing() {
        when(subscriptionRepository.findActiveExpiredBefore(any())).thenReturn(List.of());

        scheduler.expireSubscriptions();

        verifyNoInteractions(emailPort);
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void expireSubscriptions_emailFailure_doesNotAbortExpiry() {
        User user = User.builder().id("u2").email("fail@test.com").name("João").build();
        Subscription sub = Subscription.builder()
                .id("sub-2")
                .user(user)
                .planId("yearly")
                .status(SubscriptionStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().minusMinutes(10))
                .build();

        when(subscriptionRepository.findActiveExpiredBefore(any())).thenReturn(List.of(sub));
        when(subscriptionRepository.save(any())).thenReturn(sub);
        doThrow(new RuntimeException("SES error")).when(emailPort)
                .sendSubscriptionExpired(any(), any(), any(), any());

        scheduler.expireSubscriptions();

        verify(subscriptionRepository).save(argThat(s -> s.getStatus() == SubscriptionStatus.EXPIRED));
    }
}
