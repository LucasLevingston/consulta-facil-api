package com.consultafacil.api.dto.subscription;

import com.consultafacil.domain.enums.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SubscriptionResponseDTO {
    private String id;
    private String planId;
    private SubscriptionStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
