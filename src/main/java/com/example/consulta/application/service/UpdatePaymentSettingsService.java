package com.example.consulta.application.service;

import com.example.consulta.api.dto.professional.ProfessionalResponseDTO;
import com.example.consulta.api.dto.professional.UpdatePaymentSettingsDTO;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePaymentSettingsService {

    private final ProfessionalProfileRepository professionalProfileRepository;
    private final ProfessionalService professionalService;

    @Transactional
    public ProfessionalResponseDTO execute(String userId, UpdatePaymentSettingsDTO dto) {
        var professional = professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile for user: " + userId));

        professional.setPaymentTiming(dto.getPaymentTiming());
        professional.getAcceptedPaymentMethods().clear();
        professional.getAcceptedPaymentMethods().addAll(dto.getAcceptedPaymentMethods());

        professionalProfileRepository.save(professional);
        return professionalService.toResponseDTO(professional);
    }
}
