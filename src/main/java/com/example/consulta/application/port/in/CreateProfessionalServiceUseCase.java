package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.professionalservice.CreateProfessionalServiceDTO;
import com.example.consulta.api.dto.professionalservice.ProfessionalServiceResponseDTO;

public interface CreateProfessionalServiceUseCase {

    ProfessionalServiceResponseDTO execute(String userId, CreateProfessionalServiceDTO dto);
}
