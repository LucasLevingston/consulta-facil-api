package com.consultafacil.application.service.clinic;

import com.consultafacil.api.dto.clinic.ClinicResponseDTO;
import com.consultafacil.domain.port.out.clinic.ClinicRepositoryPort;
import com.consultafacil.application.port.in.clinic.GetClinicsNearbyUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetClinicsNearbyService implements GetClinicsNearbyUseCase {

    private final ClinicRepositoryPort clinicRepository;
    private final ClinicMapper clinicMapper;

    @Transactional(readOnly = true)
    public List<ClinicResponseDTO> execute(double lat, double lng, double radiusKm) {
        return clinicRepository.findNearby(lat, lng, radiusKm).stream().map(clinicMapper::toResponseDTO).toList();
    }
}
