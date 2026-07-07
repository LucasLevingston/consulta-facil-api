package com.consultafacil.application.port.in.professional.service;

import com.consultafacil.api.dto.professionalservice.CreateProfessionalServiceDTO;
import com.consultafacil.api.dto.professionalservice.ProfessionalServiceResponseDTO;

public interface CreateProfessionalServiceUseCase {

    ProfessionalServiceResponseDTO execute(String userId, CreateProfessionalServiceDTO dto);
}
