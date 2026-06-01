package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.professional.ProfessionalResponseDTO;
import com.example.consulta.api.dto.professional.UpdatePaymentSettingsDTO;

public interface UpdatePaymentSettingsUseCase {

    ProfessionalResponseDTO execute(String userId, UpdatePaymentSettingsDTO dto);
}
