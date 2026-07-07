package com.consultafacil.application.service;

import com.consultafacil.api.dto.clinic.ClinicResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.port.out.ClinicRepositoryPort;
import com.consultafacil.application.port.in.GetClinicByIdUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetClinicByIdService implements GetClinicByIdUseCase {

    private final ClinicRepositoryPort clinicRepository;
    private final ClinicMapper clinicMapper;

    @Transactional(readOnly = true)
    public ClinicResponseDTO execute(String clinicId) {
        return clinicMapper.toResponseDTO(clinicRepository.findByIdWithMembers(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId)));
    }
}
