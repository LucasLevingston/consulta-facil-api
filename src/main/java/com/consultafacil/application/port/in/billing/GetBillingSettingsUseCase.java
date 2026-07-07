package com.consultafacil.application.port.in.billing;

import com.consultafacil.api.dto.billing.settings.BillingSettingsResponseDTO;

public interface GetBillingSettingsUseCase {
    BillingSettingsResponseDTO execute();
}
