package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.professionalservice.ProfessionalServiceResponseDTO;

import java.util.List;

public interface GetProfessionalServicesUseCase {

    List<ProfessionalServiceResponseDTO> execute(String professionalId);
}
