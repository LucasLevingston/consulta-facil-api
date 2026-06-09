package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.professionalservice.ProfessionalServiceResponseDTO;

import java.util.List;

public interface GetProfessionalServicesUseCase {

    List<ProfessionalServiceResponseDTO> execute(String professionalId);
}
