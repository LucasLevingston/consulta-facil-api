package com.consultafacil.application.service.professional.profile;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.application.port.in.professional.profile.GetProfessionalByIdUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetProfessionalByIdService implements GetProfessionalByIdUseCase {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ProfessionalProfileMapper mapper;

    @Override
    @Cacheable(value = "professional-profile", key = "#professionalId")
    public ProfessionalResponseDTO execute(String professionalId) {
        log.debug("Fetching professional by ID: {}", professionalId);
        return mapper.toResponseDTO(professionalProfileRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalId)));
    }
}
