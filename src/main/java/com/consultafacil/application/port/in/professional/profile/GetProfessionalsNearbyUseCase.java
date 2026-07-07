package com.consultafacil.application.port.in.professional.profile;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;

import java.util.List;

public interface GetProfessionalsNearbyUseCase {

    List<ProfessionalResponseDTO> execute(double lat, double lng, double radiusKm,
                                           String specialty, String profession);
}
