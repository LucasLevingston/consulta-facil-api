package com.example.consulta.application.service;

import com.example.consulta.api.dto.professional.ProfessionalResponseDTO;
import com.example.consulta.api.dto.professional.UpdatePaymentSettingsDTO;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
import com.example.consulta.application.port.in.UpdatePaymentSettingsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePaymentSettingsService implements UpdatePaymentSettingsUseCase {

    private final ProfessionalProfileRepository professionalProfileRepository;
    private final ProfessionalService professionalService;

    @Transactional
    public ProfessionalResponseDTO execute(String userId, UpdatePaymentSettingsDTO dto) {
        var professional = professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile for user: " + userId));

        professional.setPaymentConfiguration(dto.getAcceptedPaymentMethods(), dto.getPaymentTiming());

        professionalProfileRepository.save(professional);
        return professionalService.toResponseDTO(professional);
    }
}
