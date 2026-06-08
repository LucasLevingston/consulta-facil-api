package com.consultafacil.application.service;

import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalService;
import com.consultafacil.domain.port.out.ProfessionalServiceRepositoryPort;
import com.consultafacil.application.port.in.DeactivateProfessionalServiceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeactivateProfessionalServiceService implements DeactivateProfessionalServiceUseCase {

    private final ProfessionalServiceRepositoryPort professionalServiceRepository;

    @CacheEvict(value = "professional-services", allEntries = true)
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
