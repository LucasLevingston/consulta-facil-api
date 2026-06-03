package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.professionalservice.ProfessionalServiceResponseDTO;
import com.example.consulta.api.dto.professionalservice.UpdateProfessionalServiceDTO;

public interface UpdateProfessionalServiceUseCase {

    ProfessionalServiceResponseDTO execute(String serviceId, String userId, UpdateProfessionalServiceDTO dto);
}
