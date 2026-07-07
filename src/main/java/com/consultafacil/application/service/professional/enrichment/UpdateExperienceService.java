package com.consultafacil.application.service.professional.enrichment;
import com.consultafacil.application.service.professional.profile.ProfessionalChildOwnershipGuard;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByUserIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileMapper;

import com.consultafacil.api.dto.professional.ProfessionalExperienceDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.application.port.in.professional.enrichment.UpdateExperienceUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalExperience;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.port.out.professional.enrichment.ProfessionalExperienceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateExperienceService implements UpdateExperienceUseCase {

    private final ProfessionalProfileByUserIdFinder profileByUserIdFinder;
    private final ProfessionalProfileByIdFinder profileByIdFinder;
    private final ProfessionalExperienceRepositoryPort experienceRepository;
    private final ProfessionalChildOwnershipGuard ownershipGuard;
    private final ProfessionalProfileMapper mapper;

    @Override
    @Transactional
    public ProfessionalResponseDTO execute(String userId, String experienceId, ProfessionalExperienceDTO dto) {
        ProfessionalProfile profile = profileByUserIdFinder.findOrThrow(userId);
        ProfessionalExperience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience", experienceId));
        ownershipGuard.assertOwnedBy(experience.getProfessionalProfile().getId(), profile.getId());
        experience.setPosition(dto.position());
        experience.setInstitution(dto.institution());
        experience.setStartYear(dto.startYear());
        experience.setEndYear(dto.endYear());
        experience.setDescription(dto.description());
        experienceRepository.save(experience);
        return mapper.toResponseDTO(profileByIdFinder.findOrThrow(profile.getId()));
    }
}
