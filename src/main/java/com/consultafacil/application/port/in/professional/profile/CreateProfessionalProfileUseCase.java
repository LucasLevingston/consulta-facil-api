package com.consultafacil.application.port.in.professional.profile;

import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;

public interface CreateProfessionalProfileUseCase {

    ProfessionalResponseDTO execute(String userId, CreateProfessionalDTO dto);
}
