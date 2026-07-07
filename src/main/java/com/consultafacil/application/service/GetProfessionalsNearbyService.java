package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.application.port.in.GetProfessionalsNearbyUseCase;
import com.consultafacil.domain.enums.ProfessionalType;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetProfessionalsNearbyService implements GetProfessionalsNearbyUseCase {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ProfessionalProfileMapper mapper;

    @Override
    public List<ProfessionalResponseDTO> execute(double lat, double lng, double radiusKm,
                                                   String specialty, String profession) {
        String specParam = resolveEnumName(Specialty.class, specialty);
        String profParam = resolveEnumName(ProfessionalType.class, profession);
        return professionalProfileRepository.findNearby(lat, lng, radiusKm, specParam, profParam)
                .stream().map(mapper::toResponseDTO).toList();
    }

    private <T extends Enum<T>> String resolveEnumName(Class<T> enumClass, String value) {
        if (value == null || value.isBlank()) return "";
        try {
            return Enum.valueOf(enumClass, value.toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            return value;
        }
    }
}
