package com.consultafacil.application.service.professional.enrichment;
import com.consultafacil.application.service.professional.profile.ProfessionalChildOwnershipGuard;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByUserIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileMapper;

import com.consultafacil.api.dto.professional.ProfessionalEducationDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.application.port.in.professional.enrichment.UpdateEducationUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalEducation;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.port.out.professional.enrichment.ProfessionalEducationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateEducationService implements UpdateEducationUseCase {

    private final ProfessionalProfileByUserIdFinder profileByUserIdFinder;
    private final ProfessionalProfileByIdFinder profileByIdFinder;
    private final ProfessionalEducationRepositoryPort educationRepository;
    private final ProfessionalChildOwnershipGuard ownershipGuard;
    private final ProfessionalProfileMapper mapper;

    @Override
    @Transactional
    public ProfessionalResponseDTO execute(String userId, String educationId, ProfessionalEducationDTO dto) {
        ProfessionalProfile profile = profileByUserIdFinder.findOrThrow(userId);
        ProfessionalEducation education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education", educationId));
        ownershipGuard.assertOwnedBy(education.getProfessionalProfile().getId(), profile.getId());
        education.setDegree(dto.degree());
        education.setInstitution(dto.institution());
        education.setFieldOfStudy(dto.fieldOfStudy());
        education.setGraduationYear(dto.graduationYear());
        educationRepository.save(education);
        return mapper.toResponseDTO(profileByIdFinder.findOrThrow(profile.getId()));
    }
}
