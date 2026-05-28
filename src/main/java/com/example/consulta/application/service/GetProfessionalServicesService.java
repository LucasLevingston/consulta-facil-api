package com.example.consulta.application.service;

import com.example.consulta.api.dto.professionalservice.ProfessionalServiceResponseDTO;
import com.example.consulta.domain.repository.ProfessionalServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetProfessionalServicesService {

    private final ProfessionalServiceRepository professionalServiceRepository;

    @Cacheable(value = "professional-services", key = "#professionalId")
    @Transactional(readOnly = true)
    public List<ProfessionalServiceResponseDTO> execute(String professionalId) {
        return professionalServiceRepository.findByProfessionalIdAndActiveTrue(professionalId)
                .stream()
                .map(CreateProfessionalServiceService::toResponseDTO)
                .toList();
    }
}
