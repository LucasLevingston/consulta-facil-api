package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.settings.BillingSettingsResponseDTO;
import com.consultafacil.api.dto.billing.settings.UpdateBillingSettingsDTO;

public interface BillingSettingsUseCase {
    BillingSettingsResponseDTO get();
    BillingSettingsResponseDTO update(UpdateBillingSettingsDTO dto);
}
