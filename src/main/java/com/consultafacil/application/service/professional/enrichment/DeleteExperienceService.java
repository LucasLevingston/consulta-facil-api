package com.consultafacil.application.service.professional.enrichment;
import com.consultafacil.application.service.professional.profile.ProfessionalChildOwnershipGuard;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByUserIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileMapper;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.application.port.in.DeleteExperienceUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalExperience;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.port.out.ProfessionalExperienceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteExperienceService implements DeleteExperienceUseCase {

    private final ProfessionalProfileByUserIdFinder profileByUserIdFinder;
    private final ProfessionalProfileByIdFinder profileByIdFinder;
    private final ProfessionalExperienceRepositoryPort experienceRepository;
    private final ProfessionalChildOwnershipGuard ownershipGuard;
    private final ProfessionalProfileMapper mapper;

    @Override
    @Transactional
    public ProfessionalResponseDTO execute(String userId, String experienceId) {
        ProfessionalProfile profile = profileByUserIdFinder.findOrThrow(userId);
        ProfessionalExperience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience", experienceId));
        ownershipGuard.assertOwnedBy(experience.getProfessionalProfile().getId(), profile.getId());
        experienceRepository.delete(experience);
        return mapper.toResponseDTO(profileByIdFinder.findOrThrow(profile.getId()));
    }
}
