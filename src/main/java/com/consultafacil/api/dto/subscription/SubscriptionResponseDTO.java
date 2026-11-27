package com.consultafacil.api.dto.subscription;

import com.consultafacil.domain.enums.SubscriptionPlan;
import com.consultafacil.domain.enums.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SubscriptionResponseDTO {
    private String id;
    private String planId;
    private SubscriptionPlan plan;
    private SubscriptionStatus status;
    private LocalDateTime expiresAt;
    private Long trialDaysRemaining; // null if no trial
    private boolean cancelAtPeriodEnd;
    private LocalDateTime createdAt;
}
