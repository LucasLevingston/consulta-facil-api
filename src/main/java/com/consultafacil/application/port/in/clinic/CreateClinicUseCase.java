package com.consultafacil.application.port.in.clinic;

import com.consultafacil.api.dto.clinic.ClinicResponseDTO;
import com.consultafacil.api.dto.clinic.CreateClinicDTO;

public interface CreateClinicUseCase {

    ClinicResponseDTO execute(String userId, CreateClinicDTO dto);
}
