package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateCouncilDTO;

public interface UpdateCouncilUseCase {

    ProfessionalResponseDTO execute(String userId, UpdateCouncilDTO dto);
}
