package com.consultafacil.application.service;

import com.consultafacil.api.dto.clinic.ClinicResponseDTO;
import com.consultafacil.domain.port.out.ClinicRepositoryPort;
import com.consultafacil.application.port.in.GetMyClinicUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetMyClinicService implements GetMyClinicUseCase {

    private final ClinicRepositoryPort clinicRepository;
    private final ClinicMapper clinicMapper;

    @Transactional(readOnly = true)
    public List<ClinicResponseDTO> execute(String userId) {
        return clinicRepository.findByOwnerId(userId).stream().map(clinicMapper::toResponseDTO).toList();
    }
}
