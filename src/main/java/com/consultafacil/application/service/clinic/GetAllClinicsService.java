package com.consultafacil.application.service.clinic;

import com.consultafacil.api.dto.clinic.ClinicResponseDTO;
import com.consultafacil.domain.port.out.clinic.ClinicRepositoryPort;
import com.consultafacil.application.port.in.clinic.GetAllClinicsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllClinicsService implements GetAllClinicsUseCase {

    private final ClinicRepositoryPort clinicRepository;
    private final ClinicMapper clinicMapper;

    @Transactional(readOnly = true)
    public List<ClinicResponseDTO> execute() {
        return clinicRepository.findByStatus("ACTIVE").stream().map(clinicMapper::toResponseDTO).toList();
    }
}
