package com.consultafacil.application.scheduler;

import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.SubscriptionPlan;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.port.out.subscription.SubscriptionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrialExpirySchedulerTest {

    @Mock SubscriptionRepositoryPort subscriptionRepository;

    TrialExpiryScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new TrialExpiryScheduler(subscriptionRepository);
    }

    private Subscription trialSubscription(String id, String userId) {
        User user = User.builder().id(userId).name("User " + userId).build();
        return Subscription.builder()
                .id(id)
                .user(user)
                .planId("trial")
                .plan(SubscriptionPlan.PROFESSIONAL)
                .status(SubscriptionStatus.ACTIVE)
                .trialEndsAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void expireTrials_expiredSubscriptions_setToFreeAndExpired() {
        Subscription sub = trialSubscription("sub-1", "u-1");
        when(subscriptionRepository.findExpiredTrialsBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(sub));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduler.expireTrials();

        verify(subscriptionRepository).save(argThat(s ->
                s.getPlan() == SubscriptionPlan.FREE && s.getStatus() == SubscriptionStatus.EXPIRED));
    }

    @Test
    void expireTrials_emptyList_noSavesCalled() {
        when(subscriptionRepository.findExpiredTrialsBefore(any(LocalDateTime.class)))
                .thenReturn(List.of());

        scheduler.expireTrials();

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void expireTrials_multipleExpired_allSaved() {
        Subscription sub1 = trialSubscription("sub-2", "u-2");
        Subscription sub2 = trialSubscription("sub-3", "u-3");
        when(subscriptionRepository.findExpiredTrialsBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(sub1, sub2));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduler.expireTrials();

        verify(subscriptionRepository, times(2)).save(any(Subscription.class));
    }

    @Test
    void expireTrials_savedSubscription_planIsFreely() {
        Subscription sub = trialSubscription("sub-4", "u-4");
        when(subscriptionRepository.findExpiredTrialsBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(sub));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduler.expireTrials();

        assertThat(sub.getPlan()).isEqualTo(SubscriptionPlan.FREE);
        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
    }
}
