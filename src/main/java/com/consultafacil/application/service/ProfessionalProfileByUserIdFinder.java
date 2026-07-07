package com.consultafacil.application.service;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfessionalProfileByUserIdFinder {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;

    public ProfessionalProfile findOrThrow(String userId) {
        return professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional profile", userId));
    }
}
