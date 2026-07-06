package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateBioDTO;
import com.consultafacil.api.dto.professional.UpdateSocialLinksDTO;
import com.consultafacil.core.exception.DuplicateResourceException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProfessionalProfileCommandService {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final UserRepositoryPort userRepository;
    private final ProfessionalProfileMapper mapper;

    @Transactional
    public ProfessionalResponseDTO createProfessionalProfile(String userId, CreateProfessionalDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (professionalProfileRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new DuplicateResourceException("Professional", "license number", dto.getLicenseNumber());
        }

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(user)
                .profession(dto.getProfession())
                .specialty(dto.getSpecialty())
                .licenseNumber(dto.getLicenseNumber())
                .status(ProfessionalProfileStatus.PENDING_REVIEW)
                .build();

        return mapper.toResponseDTO(professionalProfileRepository.save(profile));
    }

    @Transactional
    public ProfessionalResponseDTO approveApplication(String professionalId) {
        ProfessionalProfile profile = findById(professionalId);
        profile.approve();
        User user = profile.getUser();
        user.promote(UserRole.PROFESSIONAL);
        userRepository.save(user);
        return mapper.toResponseDTO(professionalProfileRepository.save(profile));
    }

    @Transactional
    public ProfessionalResponseDTO rejectApplication(String professionalId) {
        ProfessionalProfile profile = findById(professionalId);
        profile.reject();
        return mapper.toResponseDTO(professionalProfileRepository.save(profile));
    }

    @Transactional
    public ProfessionalResponseDTO updateBio(String userId, UpdateBioDTO dto) {
        ProfessionalProfile profile = findByUserId(userId);
        profile.setBio(dto.bio());
        return mapper.toResponseDTO(professionalProfileRepository.save(profile));
    }

    @Transactional
    public ProfessionalResponseDTO updateSocialLinks(String userId, UpdateSocialLinksDTO dto) {
        ProfessionalProfile profile = findByUserId(userId);
        profile.setInstagramUrl(dto.instagramUrl());
        profile.setLinkedinUrl(dto.linkedinUrl());
        profile.setWebsiteUrl(dto.websiteUrl());
        profile.setFacebookUrl(dto.facebookUrl());
        return mapper.toResponseDTO(professionalProfileRepository.save(profile));
    }

    private ProfessionalProfile findById(String professionalId) {
        return professionalProfileRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalId));
    }

    private ProfessionalProfile findByUserId(String userId) {
        return professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", userId));
    }
}
