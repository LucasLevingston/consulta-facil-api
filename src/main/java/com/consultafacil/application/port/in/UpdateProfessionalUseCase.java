package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;

public interface UpdateProfessionalUseCase {

    ProfessionalResponseDTO execute(String professionalId, CreateProfessionalDTO dto);
}
