package com.consultafacil.application.service.subscription;

import com.consultafacil.api.dto.subscription.SubscriptionResponseDTO;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.enums.SubscriptionStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionMapperTest {

    @Test
    void toDTO_copiesPublicFields() {
        LocalDateTime created = LocalDateTime.of(2026, 5, 1, 9, 0);
        Subscription subscription = Subscription.builder()
                .id("sub-1").planId("monthly").status(SubscriptionStatus.ACTIVE)
                .expiresAt(created.plusMonths(1)).createdAt(created)
                .mpPaymentId("mp-secret")
                .build();

        SubscriptionResponseDTO dto = new SubscriptionMapper().toDTO(subscription);

        assertThat(dto.getId()).isEqualTo("sub-1");
        assertThat(dto.getPlanId()).isEqualTo("monthly");
        assertThat(dto.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(dto.getExpiresAt()).isEqualTo(created.plusMonths(1));
        assertThat(dto.getCreatedAt()).isEqualTo(created);
    }
}
