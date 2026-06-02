package com.example.consulta.application.service;

import com.example.consulta.api.dto.professionalservice.CreateProfessionalServiceDTO;
import com.example.consulta.api.dto.professionalservice.ProfessionalServiceResponseDTO;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.ProfessionalProfile;
import com.example.consulta.domain.entity.ProfessionalService;
import com.example.consulta.domain.port.out.ProfessionalProfileRepositoryPort;
import com.example.consulta.domain.port.out.ProfessionalServiceRepositoryPort;
import com.example.consulta.application.port.in.CreateProfessionalServiceUseCase;
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
