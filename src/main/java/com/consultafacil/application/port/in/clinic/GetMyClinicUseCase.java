package com.consultafacil.application.port.in.clinic;

import com.consultafacil.api.dto.clinic.ClinicResponseDTO;

import java.util.List;

public interface GetMyClinicUseCase {

    List<ClinicResponseDTO> execute(String userId);
}
