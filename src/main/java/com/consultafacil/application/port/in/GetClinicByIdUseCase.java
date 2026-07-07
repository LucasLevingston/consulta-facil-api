package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.clinic.ClinicResponseDTO;

public interface GetClinicByIdUseCase {

    ClinicResponseDTO execute(String clinicId);
}
