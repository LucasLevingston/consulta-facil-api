package com.consultafacil.application.service.professional.service;

import com.consultafacil.api.dto.professionalservice.ProfessionalServiceResponseDTO;
import com.consultafacil.api.dto.professionalservice.UpdateProfessionalServiceDTO;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalService;
import com.consultafacil.domain.port.out.ProfessionalServiceRepositoryPort;
import com.consultafacil.application.port.in.UpdateProfessionalServiceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProfessionalServiceService implements UpdateProfessionalServiceUseCase {

    private final ProfessionalServiceRepositoryPort professionalServiceRepository;

    @CacheEvict(value = "professional-services", allEntries = true)
    @Transactional
    public ProfessionalServiceResponseDTO execute(String serviceId, String userId, UpdateProfessionalServiceDTO dto) {
        ProfessionalService service = professionalServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalService", serviceId));

        String ownerUserId = service.getProfessional().getUser().getId();
        if (!ownerUserId.equals(userId)) {
            throw new BadRequestException("You do not own this service");
        }

        if (dto.getName() != null) service.setName(dto.getName());
        if (dto.getDescription() != null) service.setDescription(dto.getDescription());
        if (dto.getPrice() != null) service.setPrice(dto.getPrice());
        if (dto.getDurationMinutes() != null) service.setDurationMinutes(dto.getDurationMinutes());
        if (dto.getRequiresConsultation() != null) service.setRequiresConsultation(dto.getRequiresConsultation());

        return CreateProfessionalServiceService.toResponseDTO(professionalServiceRepository.save(service));
    }
}
