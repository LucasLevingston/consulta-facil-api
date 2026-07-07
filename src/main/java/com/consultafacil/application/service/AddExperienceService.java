package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.ProfessionalExperienceDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.application.port.in.AddExperienceUseCase;
import com.consultafacil.domain.entity.ProfessionalExperience;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.port.out.ProfessionalExperienceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddExperienceService implements AddExperienceUseCase {

    private final ProfessionalProfileByUserIdFinder profileByUserIdFinder;
    private final ProfessionalProfileByIdFinder profileByIdFinder;
    private final ProfessionalExperienceRepositoryPort experienceRepository;
    private final ProfessionalProfileMapper mapper;

    @Override
    @Transactional
    public ProfessionalResponseDTO execute(String userId, ProfessionalExperienceDTO dto) {
        ProfessionalProfile profile = profileByUserIdFinder.findOrThrow(userId);
        ProfessionalExperience experience = ProfessionalExperience.builder()
                .professionalProfile(profile)
                .position(dto.position())
                .institution(dto.institution())
                .startYear(dto.startYear())
                .endYear(dto.endYear())
                .description(dto.description())
                .build();
        experienceRepository.save(experience);
        return mapper.toResponseDTO(profileByIdFinder.findOrThrow(profile.getId()));
    }
}
