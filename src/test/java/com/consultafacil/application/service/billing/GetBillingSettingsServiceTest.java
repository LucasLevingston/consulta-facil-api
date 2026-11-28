package com.consultafacil.application.service.billing;

import com.consultafacil.api.dto.billing.settings.BillingSettingsResponseDTO;
import com.consultafacil.domain.entity.BillingSettings;
import com.consultafacil.domain.port.out.billing.BillingSettingsRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBillingSettingsServiceTest {

    @Mock BillingSettingsRepositoryPort billingSettingsRepository;

    @InjectMocks GetBillingSettingsService service;

    @Test
    void execute_settingsExist_returnsDTO() {
        BillingSettings settings = BillingSettings.builder()
                .id("billing-cfg-1")
                .defaultCurrency("BRL")
                .defaultGateway("MERCADOPAGO")
                .webhookSecret("secret-123")
                .pixExpirationMinutes(45)
                .invoiceExpirationDays(14)
                .defaultTrialDays(7)
                .updatedAt(LocalDateTime.of(2025, 1, 1, 12, 0))
                .build();

        when(billingSettingsRepository.findFirst()).thenReturn(Optional.of(settings));

        BillingSettingsResponseDTO result = service.execute();

        assertThat(result.getId()).isEqualTo("billing-cfg-1");
        assertThat(result.getDefaultCurrency()).isEqualTo("BRL");
        assertThat(result.getDefaultGateway()).isEqualTo("MERCADOPAGO");
        assertThat(result.getWebhookSecret()).isEqualTo("secret-123");
        assertThat(result.getPixExpirationMinutes()).isEqualTo(45);
        assertThat(result.getInvoiceExpirationDays()).isEqualTo(14);
        assertThat(result.getDefaultTrialDays()).isEqualTo(7);
    }

    @Test
    void execute_noSettingsInDb_returnsDefaultSettings() {
        when(billingSettingsRepository.findFirst()).thenReturn(Optional.empty());

        BillingSettingsResponseDTO result = service.execute();

        assertThat(result.getId()).isEqualTo("billing-cfg-1");
        assertThat(result.getDefaultCurrency()).isEqualTo("BRL");
        assertThat(result.getDefaultGateway()).isEqualTo("MOCK");
        assertThat(result.getPixExpirationMinutes()).isEqualTo(30);
        assertThat(result.getInvoiceExpirationDays()).isEqualTo(7);
        assertThat(result.getDefaultTrialDays()).isEqualTo(14);
    }

    @Test
    void execute_settingsWithNullWebhookSecret_returnsNullSecret() {
        BillingSettings settings = BillingSettings.builder()
                .id("billing-cfg-1")
                .defaultCurrency("USD")
                .defaultGateway("STRIPE")
                .build();

        when(billingSettingsRepository.findFirst()).thenReturn(Optional.of(settings));

        BillingSettingsResponseDTO result = service.execute();

        assertThat(result.getWebhookSecret()).isNull();
        assertThat(result.getDefaultCurrency()).isEqualTo("USD");
    }

    @Test
    void toDTO_staticMethod_mapsAllFields() {
        LocalDateTime now = LocalDateTime.of(2025, 6, 15, 10, 30);
        BillingSettings settings = BillingSettings.builder()
                .id("cfg-99")
                .defaultCurrency("EUR")
                .defaultGateway("PAYPAL")
                .webhookSecret("ws-secret")
                .pixExpirationMinutes(60)
                .invoiceExpirationDays(30)
                .defaultTrialDays(0)
                .updatedAt(now)
                .build();

        BillingSettingsResponseDTO dto = GetBillingSettingsService.toDTO(settings);

        assertThat(dto.getId()).isEqualTo("cfg-99");
        assertThat(dto.getDefaultCurrency()).isEqualTo("EUR");
        assertThat(dto.getDefaultGateway()).isEqualTo("PAYPAL");
        assertThat(dto.getWebhookSecret()).isEqualTo("ws-secret");
        assertThat(dto.getPixExpirationMinutes()).isEqualTo(60);
        assertThat(dto.getInvoiceExpirationDays()).isEqualTo(30);
        assertThat(dto.getDefaultTrialDays()).isEqualTo(0);
        assertThat(dto.getUpdatedAt()).isEqualTo(now);
    }
}
