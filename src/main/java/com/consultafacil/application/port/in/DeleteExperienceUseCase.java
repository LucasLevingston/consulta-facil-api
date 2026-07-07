package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;

public interface DeleteExperienceUseCase {

    ProfessionalResponseDTO execute(String userId, String experienceId);
}
