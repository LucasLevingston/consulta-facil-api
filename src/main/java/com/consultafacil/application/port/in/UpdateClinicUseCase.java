package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.clinic.ClinicResponseDTO;
import com.consultafacil.api.dto.clinic.CreateClinicDTO;

public interface UpdateClinicUseCase {

    ClinicResponseDTO execute(String clinicId, String userId, CreateClinicDTO dto);
}
