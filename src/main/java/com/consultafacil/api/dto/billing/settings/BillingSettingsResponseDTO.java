package com.consultafacil.api.dto.billing.settings;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BillingSettingsResponseDTO {
    private String id;
    private String defaultCurrency;
    private String defaultGateway;
    private String webhookSecret;
    private int pixExpirationMinutes;
    private int invoiceExpirationDays;
    private int defaultTrialDays;
    private LocalDateTime updatedAt;
}
