package com.consultafacil.application.service.subscription;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.enums.SubscriptionPlan;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.exception.InvalidStateException;
import com.consultafacil.domain.port.out.subscription.SubscriptionRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelSubscriptionServiceTest {

    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;

    @InjectMocks
    private CancelSubscriptionService cancelSubscriptionService;

    @Test
    void cancel_setsFlag_andSaves() {
        Subscription sub = Subscription.builder()
                .plan(SubscriptionPlan.PROFESSIONAL)
                .status(SubscriptionStatus.ACTIVE)
                .cancelAtPeriodEnd(false)
                .build();
        when(subscriptionRepository.findByUserId("u1")).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any())).thenReturn(sub);

        cancelSubscriptionService.execute("u1");

        assertTrue(sub.isCancelAtPeriodEnd());
        verify(subscriptionRepository).save(sub);
    }

    @Test
    void cancel_alreadyCancelled_throws() {
        Subscription sub = Subscription.builder()
                .plan(SubscriptionPlan.FREE)
                .status(SubscriptionStatus.CANCELLED)
                .build();
        when(subscriptionRepository.findByUserId("u1")).thenReturn(Optional.of(sub));

        assertThrows(InvalidStateException.class, () -> cancelSubscriptionService.execute("u1"));
    }

    @Test
    void cancel_notFound_throws() {
        when(subscriptionRepository.findByUserId("u1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> cancelSubscriptionService.execute("u1"));
    }
}
