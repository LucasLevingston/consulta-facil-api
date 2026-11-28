package com.consultafacil.application.service.billing;

import com.consultafacil.api.dto.billing.settings.BillingSettingsResponseDTO;
import com.consultafacil.api.dto.billing.settings.UpdateBillingSettingsDTO;
import com.consultafacil.domain.entity.BillingSettings;
import com.consultafacil.domain.port.out.billing.BillingSettingsRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateBillingSettingsServiceTest {

    @Mock BillingSettingsRepositoryPort billingSettingsRepository;

    @InjectMocks UpdateBillingSettingsService service;

    private BillingSettings defaultSettings() {
        return BillingSettings.builder()
                .id("billing-cfg-1")
                .defaultCurrency("BRL")
                .defaultGateway("MOCK")
                .pixExpirationMinutes(30)
                .invoiceExpirationDays(7)
                .defaultTrialDays(14)
                .build();
    }

    @Test
    void execute_existingSettings_updatesAndReturnsDTO() {
        BillingSettings existing = defaultSettings();
        when(billingSettingsRepository.findFirst()).thenReturn(Optional.of(existing));
        when(billingSettingsRepository.save(any())).thenReturn(existing);

        UpdateBillingSettingsDTO dto = new UpdateBillingSettingsDTO();
        dto.setDefaultCurrency("USD");
        dto.setDefaultGateway("STRIPE");

        BillingSettingsResponseDTO result = service.execute(dto);

        assertThat(result).isNotNull();
        ArgumentCaptor<BillingSettings> captor = ArgumentCaptor.forClass(BillingSettings.class);
        verify(billingSettingsRepository).save(captor.capture());
        assertThat(captor.getValue().getDefaultCurrency()).isEqualTo("USD");
        assertThat(captor.getValue().getDefaultGateway()).isEqualTo("STRIPE");
    }

    @Test
    void execute_noExistingSettings_createsNewWithDefaults() {
        when(billingSettingsRepository.findFirst()).thenReturn(Optional.empty());
        BillingSettings newSettings = BillingSettings.builder()
                .id("billing-cfg-1")
                .defaultCurrency("BRL")
                .defaultGateway("MOCK")
                .pixExpirationMinutes(30)
                .invoiceExpirationDays(7)
                .defaultTrialDays(14)
                .build();
        when(billingSettingsRepository.save(any())).thenReturn(newSettings);

        UpdateBillingSettingsDTO dto = new UpdateBillingSettingsDTO();
        dto.setWebhookSecret("new-secret");

        BillingSettingsResponseDTO result = service.execute(dto);

        assertThat(result).isNotNull();
        ArgumentCaptor<BillingSettings> captor = ArgumentCaptor.forClass(BillingSettings.class);
        verify(billingSettingsRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo("billing-cfg-1");
        assertThat(captor.getValue().getWebhookSecret()).isEqualTo("new-secret");
    }

    @Test
    void execute_allNullFields_doesNotOverwriteExisting() {
        BillingSettings existing = defaultSettings();
        existing.setWebhookSecret("original-secret");
        when(billingSettingsRepository.findFirst()).thenReturn(Optional.of(existing));
        when(billingSettingsRepository.save(any())).thenReturn(existing);

        UpdateBillingSettingsDTO dto = new UpdateBillingSettingsDTO();

        service.execute(dto);

        ArgumentCaptor<BillingSettings> captor = ArgumentCaptor.forClass(BillingSettings.class);
        verify(billingSettingsRepository).save(captor.capture());
        BillingSettings saved = captor.getValue();
        assertThat(saved.getDefaultCurrency()).isEqualTo("BRL");
        assertThat(saved.getDefaultGateway()).isEqualTo("MOCK");
        assertThat(saved.getWebhookSecret()).isEqualTo("original-secret");
        assertThat(saved.getPixExpirationMinutes()).isEqualTo(30);
        assertThat(saved.getInvoiceExpirationDays()).isEqualTo(7);
        assertThat(saved.getDefaultTrialDays()).isEqualTo(14);
    }

    @Test
    void execute_updatePixAndInvoiceExpiration_savesUpdatedValues() {
        BillingSettings existing = defaultSettings();
        when(billingSettingsRepository.findFirst()).thenReturn(Optional.of(existing));
        when(billingSettingsRepository.save(any())).thenReturn(existing);

        UpdateBillingSettingsDTO dto = new UpdateBillingSettingsDTO();
        dto.setPixExpirationMinutes(60);
        dto.setInvoiceExpirationDays(30);
        dto.setDefaultTrialDays(0);

        service.execute(dto);

        ArgumentCaptor<BillingSettings> captor = ArgumentCaptor.forClass(BillingSettings.class);
        verify(billingSettingsRepository).save(captor.capture());
        BillingSettings saved = captor.getValue();
        assertThat(saved.getPixExpirationMinutes()).isEqualTo(60);
        assertThat(saved.getInvoiceExpirationDays()).isEqualTo(30);
        assertThat(saved.getDefaultTrialDays()).isEqualTo(0);
    }
}
