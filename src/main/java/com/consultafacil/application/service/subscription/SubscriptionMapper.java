package com.consultafacil.application.service.subscription;

import com.consultafacil.api.dto.subscription.SubscriptionResponseDTO;
import com.consultafacil.domain.entity.Subscription;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class SubscriptionMapper {

    public SubscriptionResponseDTO toDTO(Subscription s) {
        Long trialDaysRemaining = null;
        if (s.getTrialEndsAt() != null && s.getTrialEndsAt().isAfter(LocalDateTime.now())) {
            trialDaysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), s.getTrialEndsAt());
        }
        return SubscriptionResponseDTO.builder()
                .id(s.getId())
                .planId(s.getPlanId())
                .plan(s.getPlan())
                .status(s.getStatus())
                .expiresAt(s.getExpiresAt())
                .trialDaysRemaining(trialDaysRemaining)
                .cancelAtPeriodEnd(s.isCancelAtPeriodEnd())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
