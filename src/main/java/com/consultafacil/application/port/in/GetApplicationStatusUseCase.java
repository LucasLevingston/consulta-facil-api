package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;

public interface GetApplicationStatusUseCase {

    ProfessionalResponseDTO execute(String userId);
}
