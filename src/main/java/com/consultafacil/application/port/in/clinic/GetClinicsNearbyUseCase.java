package com.consultafacil.application.port.in.clinic;

import com.consultafacil.api.dto.clinic.ClinicResponseDTO;

import java.util.List;

public interface GetClinicsNearbyUseCase {

    List<ClinicResponseDTO> execute(double lat, double lng, double radiusKm);
}
