package com.consultafacil.application.port.in.billing;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdatePaymentSettingsDTO;

public interface UpdatePaymentSettingsUseCase {

    ProfessionalResponseDTO execute(String userId, UpdatePaymentSettingsDTO dto);
}
