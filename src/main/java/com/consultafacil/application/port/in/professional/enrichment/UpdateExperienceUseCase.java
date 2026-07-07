package com.consultafacil.application.port.in.professional.enrichment;

import com.consultafacil.api.dto.professional.ProfessionalExperienceDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;

public interface UpdateExperienceUseCase {

    ProfessionalResponseDTO execute(String userId, String experienceId, ProfessionalExperienceDTO dto);
}
