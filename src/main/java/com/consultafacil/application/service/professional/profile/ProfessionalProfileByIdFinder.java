package com.consultafacil.application.service.professional.profile;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfessionalProfileByIdFinder {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;

    public ProfessionalProfile findOrThrow(String profileId) {
        return professionalProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional profile", profileId));
    }
}
