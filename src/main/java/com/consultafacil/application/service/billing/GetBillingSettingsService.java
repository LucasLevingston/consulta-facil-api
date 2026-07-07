package com.consultafacil.application.service.billing;

import com.consultafacil.api.dto.billing.settings.BillingSettingsResponseDTO;
import com.consultafacil.application.port.in.GetBillingSettingsUseCase;
import com.consultafacil.domain.entity.BillingSettings;
import com.consultafacil.domain.port.out.BillingSettingsRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetBillingSettingsService implements GetBillingSettingsUseCase {

    private final BillingSettingsRepositoryPort billingSettingsRepository;

    @Override
    @Transactional(readOnly = true)
    public BillingSettingsResponseDTO execute() {
        BillingSettings settings = billingSettingsRepository.findFirst()
                .orElseGet(() -> BillingSettings.builder().id("billing-cfg-1").build());
        return toDTO(settings);
    }

    static BillingSettingsResponseDTO toDTO(BillingSettings s) {
        return BillingSettingsResponseDTO.builder()
                .id(s.getId())
                .defaultCurrency(s.getDefaultCurrency())
                .defaultGateway(s.getDefaultGateway())
                .webhookSecret(s.getWebhookSecret())
                .pixExpirationMinutes(s.getPixExpirationMinutes())
                .invoiceExpirationDays(s.getInvoiceExpirationDays())
                .defaultTrialDays(s.getDefaultTrialDays())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
