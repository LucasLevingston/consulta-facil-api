package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.core.exception.DuplicateResourceException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProfessionalProfileAdminCommandService {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final UserRepositoryPort userRepository;
    private final ProfessionalProfileMapper mapper;

    @Caching(evict = {
        @CacheEvict(value = "professional-profile", key = "#professionalId"),
        @CacheEvict(value = "professional-services", allEntries = true)
    })
    @Transactional
    public ProfessionalResponseDTO updateProfessional(String professionalId, CreateProfessionalDTO dto) {
        ProfessionalProfile profile = findById(professionalId);

        if (!profile.getLicenseNumber().equals(dto.getLicenseNumber()) &&
                professionalProfileRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new DuplicateResourceException("Professional", "license number", dto.getLicenseNumber());
        }

        profile.setProfession(dto.getProfession());
        profile.setSpecialty(dto.getSpecialty());
        profile.setLicenseNumber(dto.getLicenseNumber());

        return mapper.toResponseDTO(professionalProfileRepository.save(profile));
    }

    @Caching(evict = {
        @CacheEvict(value = "professional-profile", key = "#professionalId"),
        @CacheEvict(value = "professional-services", allEntries = true)
    })
    @Transactional
    public void deleteProfessional(String professionalId) {
        ProfessionalProfile profile = findById(professionalId);
        profile.getUser().promote(UserRole.PATIENT);
        userRepository.save(profile.getUser());
        professionalProfileRepository.delete(profile);
    }

    private ProfessionalProfile findById(String professionalId) {
        return professionalProfileRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalId));
    }
}
