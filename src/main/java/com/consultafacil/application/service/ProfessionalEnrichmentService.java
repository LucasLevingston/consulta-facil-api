package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.ProfessionalCertificateDTO;
import com.consultafacil.api.dto.professional.ProfessionalEducationDTO;
import com.consultafacil.api.dto.professional.ProfessionalExperienceDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateAddressDTO;
import com.consultafacil.api.dto.professional.UpdateCouncilDTO;
import com.consultafacil.application.port.in.ProfessionalEnrichmentUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalCertificate;
import com.consultafacil.domain.entity.ProfessionalEducation;
import com.consultafacil.domain.entity.ProfessionalExperience;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.port.out.ProfessionalCertificateRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalEducationRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalExperienceRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProfessionalEnrichmentService implements ProfessionalEnrichmentUseCase {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ProfessionalEducationRepositoryPort educationRepository;
    private final ProfessionalExperienceRepositoryPort experienceRepository;
    private final ProfessionalCertificateRepositoryPort certificateRepository;
    private final ProfessionalService professionalService;

    @Override
    @Transactional
    public ProfessionalResponseDTO updateCouncil(String userId, UpdateCouncilDTO dto) {
        ProfessionalProfile profile = findByUserId(userId);
        profile.setCouncilType(dto.councilType());
        profile.setCouncilState(dto.councilState());
        return professionalService.toResponseDTO(professionalProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public ProfessionalResponseDTO updateAddress(String userId, UpdateAddressDTO dto) {
        ProfessionalProfile profile = findByUserId(userId);
        if (dto.city() != null) profile.setCity(dto.city());
        if (dto.state() != null) profile.setState(dto.state());
        if (dto.address() != null) profile.setAddress(dto.address());
        if (dto.zipCode() != null) profile.setZipCode(dto.zipCode());
        if (dto.neighborhood() != null) profile.setNeighborhood(dto.neighborhood());
        if (dto.streetNumber() != null) profile.setStreetNumber(dto.streetNumber());
        if (dto.complement() != null) profile.setComplement(dto.complement());
        if (dto.latitude() != null) profile.setLatitude(dto.latitude());
        if (dto.longitude() != null) profile.setLongitude(dto.longitude());
        return professionalService.toResponseDTO(professionalProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public ProfessionalResponseDTO addEducation(String userId, ProfessionalEducationDTO dto) {
        ProfessionalProfile profile = findByUserId(userId);
        ProfessionalEducation education = ProfessionalEducation.builder()
                .professionalProfile(profile)
                .degree(dto.degree())
                .institution(dto.institution())
                .fieldOfStudy(dto.fieldOfStudy())
                .graduationYear(dto.graduationYear())
                .build();
        educationRepository.save(education);
        return professionalService.toResponseDTO(findById(profile.getId()));
    }

    @Override
    @Transactional
    public ProfessionalResponseDTO updateEducation(String userId, String educationId, ProfessionalEducationDTO dto) {
        ProfessionalProfile profile = findByUserId(userId);
        ProfessionalEducation education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education", educationId));
        assertOwnership(education.getProfessionalProfile().getId(), profile.getId());
        education.setDegree(dto.degree());
        education.setInstitution(dto.institution());
        education.setFieldOfStudy(dto.fieldOfStudy());
        education.setGraduationYear(dto.graduationYear());
        educationRepository.save(education);
        return professionalService.toResponseDTO(findById(profile.getId()));
    }

    @Override
    @Transactional
    public ProfessionalResponseDTO deleteEducation(String userId, String educationId) {
        ProfessionalProfile profile = findByUserId(userId);
        ProfessionalEducation education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education", educationId));
        assertOwnership(education.getProfessionalProfile().getId(), profile.getId());
        educationRepository.delete(education);
        return professionalService.toResponseDTO(findById(profile.getId()));
    }

    @Override
    @Transactional
    public ProfessionalResponseDTO addExperience(String userId, ProfessionalExperienceDTO dto) {
        ProfessionalProfile profile = findByUserId(userId);
        ProfessionalExperience experience = ProfessionalExperience.builder()
                .professionalProfile(profile)
                .position(dto.position())
                .institution(dto.institution())
                .startYear(dto.startYear())
                .endYear(dto.endYear())
                .description(dto.description())
                .build();
        experienceRepository.save(experience);
        return professionalService.toResponseDTO(findById(profile.getId()));
    }

    @Override
    @Transactional
    public ProfessionalResponseDTO updateExperience(String userId, String experienceId, ProfessionalExperienceDTO dto) {
        ProfessionalProfile profile = findByUserId(userId);
        ProfessionalExperience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience", experienceId));
        assertOwnership(experience.getProfessionalProfile().getId(), profile.getId());
        experience.setPosition(dto.position());
        experience.setInstitution(dto.institution());
        experience.setStartYear(dto.startYear());
        experience.setEndYear(dto.endYear());
        experience.setDescription(dto.description());
        experienceRepository.save(experience);
        return professionalService.toResponseDTO(findById(profile.getId()));
    }

    @Override
    @Transactional
    public ProfessionalResponseDTO deleteExperience(String userId, String experienceId) {
        ProfessionalProfile profile = findByUserId(userId);
        ProfessionalExperience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience", experienceId));
        assertOwnership(experience.getProfessionalProfile().getId(), profile.getId());
        experienceRepository.delete(experience);
        return professionalService.toResponseDTO(findById(profile.getId()));
    }

    @Override
    @Transactional
    public ProfessionalResponseDTO addCertificate(String userId, ProfessionalCertificateDTO dto) {
        ProfessionalProfile profile = findByUserId(userId);
        ProfessionalCertificate certificate = ProfessionalCertificate.builder()
                .professionalProfile(profile)
                .title(dto.title())
                .issuingOrganization(dto.issuingOrganization())
                .issueYear(dto.issueYear())
                .certificateUrl(dto.certificateUrl())
                .build();
        certificateRepository.save(certificate);
        return professionalService.toResponseDTO(findById(profile.getId()));
    }

    @Override
    @Transactional
    public ProfessionalResponseDTO updateCertificate(String userId, String certificateId, ProfessionalCertificateDTO dto) {
        ProfessionalProfile profile = findByUserId(userId);
        ProfessionalCertificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate", certificateId));
        assertOwnership(certificate.getProfessionalProfile().getId(), profile.getId());
        certificate.setTitle(dto.title());
        certificate.setIssuingOrganization(dto.issuingOrganization());
        certificate.setIssueYear(dto.issueYear());
        certificate.setCertificateUrl(dto.certificateUrl());
        certificateRepository.save(certificate);
        return professionalService.toResponseDTO(findById(profile.getId()));
    }

    @Override
    @Transactional
    public ProfessionalResponseDTO deleteCertificate(String userId, String certificateId) {
        ProfessionalProfile profile = findByUserId(userId);
        ProfessionalCertificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate", certificateId));
        assertOwnership(certificate.getProfessionalProfile().getId(), profile.getId());
        certificateRepository.delete(certificate);
        return professionalService.toResponseDTO(findById(profile.getId()));
    }

    private ProfessionalProfile findByUserId(String userId) {
        return professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional profile", userId));
    }

    private ProfessionalProfile findById(String profileId) {
        return professionalProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional profile", profileId));
    }

    private void assertOwnership(String ownerProfileId, String requestorProfileId) {
        if (!ownerProfileId.equals(requestorProfileId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
        }
    }
}
