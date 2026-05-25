package com.example.consulta.application.service;

import com.example.consulta.api.dto.professionalservice.ProfessionalServiceResponseDTO;
import com.example.consulta.api.dto.professionalservice.UpdateProfessionalServiceDTO;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.ProfessionalService;
import com.example.consulta.domain.repository.ProfessionalServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProfessionalServiceService {

    private final ProfessionalServiceRepository professionalServiceRepository;

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
