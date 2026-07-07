package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.application.port.in.GetProfessionalByUserIdUseCase;
import com.consultafacil.application.port.in.SetConsultationPriceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SetConsultationPriceService implements SetConsultationPriceUseCase {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final GetProfessionalByUserIdUseCase getProfessionalByUserId;

    @Transactional
    public ProfessionalResponseDTO execute(String userId, BigDecimal price) {
        ProfessionalProfile profile = professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile not found for user: " + userId));

        profile.updateConsultationPrice(price);
        professionalProfileRepository.save(profile);
        return getProfessionalByUserId.execute(userId);
    }
}
