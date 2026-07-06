package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.ProfessionalType;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfessionalProfileQueryService {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ProfessionalProfileMapper mapper;
    private final ProfessionalProfileSummaryMapper summaryMapper;

    @Cacheable(value = "professional-profile", key = "#professionalId")
    public ProfessionalResponseDTO getProfessionalById(String professionalId) {
        log.debug("Fetching professional by ID: {}", professionalId);
        return mapper.toResponseDTO(professionalProfileRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalId)));
    }

    public ProfessionalResponseDTO getProfessionalByUserId(String userId) {
        return mapper.toResponseDTO(professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Professional profile not found for user: " + userId)));
    }

    public Page<ProfessionalResponseDTO> searchBySpecialty(String specialty, Pageable pageable) {
        try {
            Specialty specialtyEnum = Specialty.valueOf(specialty.toUpperCase());
            return professionalProfileRepository
                    .findBySpecialtyAndStatus(specialtyEnum, ProfessionalProfileStatus.ACTIVE, pageable)
                    .map(mapper::toResponseDTO);
        } catch (IllegalArgumentException e) {
            return Page.empty(pageable);
        }
    }

    public Page<ProfessionalResponseDTO> getAllProfessionals(String profession, String specialty,
                                                              String name, Pageable pageable) {
        String profParam = resolveEnumName(ProfessionalType.class, profession);
        String specParam = resolveEnumName(Specialty.class, specialty);
        String nameParam = (name != null && !name.isBlank()) ? name : "";
        return professionalProfileRepository.findActiveWithFilters(profParam, specParam, nameParam, pageable)
                .map(summaryMapper::toListSummaryDTO);
    }

    public List<ProfessionalResponseDTO> getProfessionalsNearby(double lat, double lng, double radiusKm,
                                                                 String specialty, String profession) {
        String specParam = resolveEnumName(Specialty.class, specialty);
        String profParam = resolveEnumName(ProfessionalType.class, profession);
        return professionalProfileRepository.findNearby(lat, lng, radiusKm, specParam, profParam)
                .stream().map(mapper::toResponseDTO).toList();
    }

    public Page<ProfessionalResponseDTO> getPendingApplications(Pageable pageable) {
        return professionalProfileRepository.findByStatus(ProfessionalProfileStatus.PENDING_REVIEW, pageable)
                .map(mapper::toResponseDTO);
    }

    public ProfessionalResponseDTO getApplicationStatus(String userId) {
        return mapper.toResponseDTO(professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Professional application not found for user: " + userId)));
    }

    private <T extends Enum<T>> String resolveEnumName(Class<T> enumClass, String value) {
        if (value == null || value.isBlank()) return "";
        try {
            return Enum.valueOf(enumClass, value.toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            return value;
        }
    }
}
