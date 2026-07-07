package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.application.port.in.DeleteEducationUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalEducation;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.port.out.ProfessionalEducationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteEducationService implements DeleteEducationUseCase {

    private final ProfessionalProfileByUserIdFinder profileByUserIdFinder;
    private final ProfessionalProfileByIdFinder profileByIdFinder;
    private final ProfessionalEducationRepositoryPort educationRepository;
    private final ProfessionalChildOwnershipGuard ownershipGuard;
    private final ProfessionalProfileMapper mapper;

    @Override
    @Transactional
    public ProfessionalResponseDTO execute(String userId, String educationId) {
        ProfessionalProfile profile = profileByUserIdFinder.findOrThrow(userId);
        ProfessionalEducation education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education", educationId));
        ownershipGuard.assertOwnedBy(education.getProfessionalProfile().getId(), profile.getId());
        educationRepository.delete(education);
        return mapper.toResponseDTO(profileByIdFinder.findOrThrow(profile.getId()));
    }
}
