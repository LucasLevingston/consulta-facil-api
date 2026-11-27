package com.consultafacil.application.service.subscription;

import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.SubscriptionPlan;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.port.out.subscription.SubscriptionRepositoryPort;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitiateProfessionalTrialServiceTest {

    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private InitiateProfessionalTrialService initiateTrialService;

    @Test
    void newUser_createsTrial() {
        User user = User.builder().id("u1").email("a@b.com").name("Test").build();
        when(subscriptionRepository.findByUserId("u1")).thenReturn(Optional.empty());
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        initiateTrialService.execute("u1");

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        Subscription saved = captor.getValue();
        assertEquals(SubscriptionStatus.ACTIVE, saved.getStatus());
        assertNotNull(saved.getTrialEndsAt());
        assertTrue(saved.getTrialEndsAt().isAfter(java.time.LocalDateTime.now().plusDays(13)));
    }

    @Test
    void existingSubscription_skips() {
        Subscription existing = Subscription.builder().plan(SubscriptionPlan.PROFESSIONAL).build();
        when(subscriptionRepository.findByUserId("u1")).thenReturn(Optional.of(existing));

        initiateTrialService.execute("u1");

        verify(subscriptionRepository, never()).save(any());
    }
}
