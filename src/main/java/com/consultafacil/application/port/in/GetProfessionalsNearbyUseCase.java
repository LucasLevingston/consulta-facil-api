package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;

import java.util.List;

public interface GetProfessionalsNearbyUseCase {

    List<ProfessionalResponseDTO> execute(double lat, double lng, double radiusKm,
                                           String specialty, String profession);
}
