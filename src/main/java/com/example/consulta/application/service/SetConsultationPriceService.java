package com.example.consulta.application.service;

import com.example.consulta.api.dto.professional.ProfessionalResponseDTO;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.ProfessionalProfile;
import com.example.consulta.domain.port.out.ProfessionalProfileRepositoryPort;
import com.example.consulta.application.port.in.SetConsultationPriceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SetConsultationPriceService implements SetConsultationPriceUseCase {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ProfessionalService professionalService;

    @Transactional
    public ProfessionalResponseDTO execute(String userId, BigDecimal price) {
        ProfessionalProfile profile = professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile not found for user: " + userId));

        profile.updateConsultationPrice(price);
        professionalProfileRepository.save(profile);
        return professionalService.getProfessionalByUserId(userId);
    }
}
