package com.consultafacil.application.port.in.professional.profile;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;

public interface RejectApplicationUseCase {

    ProfessionalResponseDTO execute(String professionalId);
}
