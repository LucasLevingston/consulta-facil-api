package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.professional.ProfessionalEducationDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;

public interface UpdateEducationUseCase {

    ProfessionalResponseDTO execute(String userId, String educationId, ProfessionalEducationDTO dto);
}
