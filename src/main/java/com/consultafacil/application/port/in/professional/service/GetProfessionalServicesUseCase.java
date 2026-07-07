package com.consultafacil.application.port.in.professional.service;

import com.consultafacil.api.dto.professionalservice.ProfessionalServiceResponseDTO;

import java.util.List;

public interface GetProfessionalServicesUseCase {

    List<ProfessionalServiceResponseDTO> execute(String professionalId);
}
