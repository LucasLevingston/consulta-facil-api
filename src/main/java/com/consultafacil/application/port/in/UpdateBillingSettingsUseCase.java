package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.settings.BillingSettingsResponseDTO;
import com.consultafacil.api.dto.billing.settings.UpdateBillingSettingsDTO;

public interface UpdateBillingSettingsUseCase {
    BillingSettingsResponseDTO execute(UpdateBillingSettingsDTO dto);
}
