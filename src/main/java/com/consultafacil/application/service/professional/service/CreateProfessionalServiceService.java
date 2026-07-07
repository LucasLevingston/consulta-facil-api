package com.consultafacil.application.service.professional.service;

import com.consultafacil.api.dto.professionalservice.CreateProfessionalServiceDTO;
import com.consultafacil.api.dto.professionalservice.ProfessionalServiceResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.ProfessionalService;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import com.consultafacil.domain.port.out.professional.service.ProfessionalServiceRepositoryPort;
import com.consultafacil.application.port.in.professional.service.CreateProfessionalServiceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateProfessionalServiceService implements CreateProfessionalServiceUseCase {

    private final ProfessionalServiceRepositoryPort professionalServiceRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;

    @CacheEvict(value = "professional-services", allEntries = true)
    @Transactional
    public ProfessionalServiceResponseDTO execute(String userId, CreateProfessionalServiceDTO dto) {
        ProfessionalProfile profile = professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile not found for user: " + userId));

        ProfessionalService service = ProfessionalService.builder()
                .professional(profile)
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .durationMinutes(dto.getDurationMinutes())
                .requiresConsultation(dto.isRequiresConsultation())
                .build();

        return toResponseDTO(professionalServiceRepository.save(service));
    }

    static ProfessionalServiceResponseDTO toResponseDTO(ProfessionalService s) {
        return ProfessionalServiceResponseDTO.builder()
                .id(s.getId())
                .professionalId(s.getProfessional().getId())
                .professionalName(s.getProfessional().getUser().getName())
                .name(s.getName())
                .description(s.getDescription())
                .price(s.getPrice())
                .durationMinutes(s.getDurationMinutes())
                .requiresConsultation(s.isRequiresConsultation())
                .active(s.isActive())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
