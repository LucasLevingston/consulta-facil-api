package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.settings.BillingSettingsResponseDTO;
import com.consultafacil.api.dto.billing.settings.UpdateBillingSettingsDTO;
import com.consultafacil.application.port.in.BillingSettingsUseCase;
import com.consultafacil.domain.entity.BillingSettings;
import com.consultafacil.domain.port.out.BillingSettingsRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillingSettingsService implements BillingSettingsUseCase {

    private final BillingSettingsRepositoryPort billingSettingsRepository;

    @Override
    @Transactional(readOnly = true)
    public BillingSettingsResponseDTO get() {
        BillingSettings settings = billingSettingsRepository.findFirst()
                .orElseGet(() -> BillingSettings.builder().id("billing-cfg-1").build());
        return toDTO(settings);
    }

    @Override
    @Transactional
    public BillingSettingsResponseDTO update(UpdateBillingSettingsDTO dto) {
        BillingSettings settings = billingSettingsRepository.findFirst()
                .orElseGet(() -> BillingSettings.builder().id("billing-cfg-1").build());
        if (dto.getDefaultCurrency() != null) settings.setDefaultCurrency(dto.getDefaultCurrency());
        if (dto.getDefaultGateway() != null) settings.setDefaultGateway(dto.getDefaultGateway());
        if (dto.getWebhookSecret() != null) settings.setWebhookSecret(dto.getWebhookSecret());
        if (dto.getPixExpirationMinutes() != null) settings.setPixExpirationMinutes(dto.getPixExpirationMinutes());
        if (dto.getInvoiceExpirationDays() != null) settings.setInvoiceExpirationDays(dto.getInvoiceExpirationDays());
        if (dto.getDefaultTrialDays() != null) settings.setDefaultTrialDays(dto.getDefaultTrialDays());
        return toDTO(billingSettingsRepository.save(settings));
    }

    private BillingSettingsResponseDTO toDTO(BillingSettings s) {
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
