package com.consultafacil.api.dto.billing.settings;

import lombok.Data;

@Data
public class UpdateBillingSettingsDTO {
    private String defaultCurrency;
    private String defaultGateway;
    private String webhookSecret;
    private Integer pixExpirationMinutes;
    private Integer invoiceExpirationDays;
    private Integer defaultTrialDays;
}
