package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.professional.ProfessionalExperienceDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;

public interface AddExperienceUseCase {

    ProfessionalResponseDTO execute(String userId, ProfessionalExperienceDTO dto);
}
