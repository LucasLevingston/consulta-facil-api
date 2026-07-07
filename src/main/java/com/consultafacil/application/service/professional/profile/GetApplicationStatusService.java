package com.consultafacil.application.service.professional.profile;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.application.port.in.professional.profile.GetApplicationStatusUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetApplicationStatusService implements GetApplicationStatusUseCase {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ProfessionalProfileMapper mapper;

    @Override
    public ProfessionalResponseDTO execute(String userId) {
        return mapper.toResponseDTO(professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Professional application not found for user: " + userId)));
    }
}
