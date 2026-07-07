package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.professional.ProfessionalEducationDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;

public interface AddEducationUseCase {

    ProfessionalResponseDTO execute(String userId, ProfessionalEducationDTO dto);
}
