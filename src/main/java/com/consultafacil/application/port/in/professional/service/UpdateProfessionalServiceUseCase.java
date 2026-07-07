package com.consultafacil.application.port.in.professional.service;

import com.consultafacil.api.dto.professionalservice.ProfessionalServiceResponseDTO;
import com.consultafacil.api.dto.professionalservice.UpdateProfessionalServiceDTO;

public interface UpdateProfessionalServiceUseCase {

    ProfessionalServiceResponseDTO execute(String serviceId, String userId, UpdateProfessionalServiceDTO dto);
}
