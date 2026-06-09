package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdatePaymentSettingsDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.application.port.in.UpdatePaymentSettingsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePaymentSettingsService implements UpdatePaymentSettingsUseCase {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
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
