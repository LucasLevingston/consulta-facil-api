package com.consultafacil.application.service.subscription;

import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.SubscriptionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMySubscriptionServiceTest {

    @Mock SubscriptionRepositoryPort subscriptionRepository;

    GetMySubscriptionService service;
    Subscription subscription;

    @BeforeEach
    void setUp() {
        service = new GetMySubscriptionService(subscriptionRepository, new SubscriptionMapper());

        User user = User.builder().id("u-1").email("joao@email.com").name("João Silva").password("x").role(UserRole.PROFESSIONAL).build();
        subscription = Subscription.builder().id("sub-1").user(user).planId("monthly")
                .status(SubscriptionStatus.ACTIVE).expiresAt(LocalDateTime.now().plusDays(30)).build();
    }

    @Test
    void getMySubscription_existingSubscription_returnsDTO() {
        when(subscriptionRepository.findByUserId("u-1")).thenReturn(Optional.of(subscription));
        var result = service.execute("u-1");
        assertThat(result).isPresent();
        assertThat(result.get().getPlanId()).isEqualTo("monthly");
        assertThat(result.get().getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void getMySubscription_noSubscription_returnsEmpty() {
        when(subscriptionRepository.findByUserId("u-1")).thenReturn(Optional.empty());
        var result = service.execute("u-1");
        assertThat(result).isEmpty();
    }
}
