package com.example.consulta.application.service;

import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.ProfessionalService;
import com.example.consulta.domain.repository.ProfessionalServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeactivateProfessionalServiceService {

    private final ProfessionalServiceRepository professionalServiceRepository;

    @Transactional
    public void execute(String serviceId, String userId) {
        ProfessionalService service = professionalServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalService", serviceId));

        if (!service.getProfessional().getUser().getId().equals(userId)) {
            throw new BadRequestException("You do not own this service");
        }

        service.setActive(false);
        professionalServiceRepository.save(service);
    }
}
