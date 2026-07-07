package com.consultafacil.application.service.professional.profile;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateBioDTO;
import com.consultafacil.application.port.in.UpdateBioUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateBioService implements UpdateBioUseCase {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ProfessionalProfileMapper mapper;

    @Override
    @Transactional
    public ProfessionalResponseDTO execute(String userId, UpdateBioDTO dto) {
        ProfessionalProfile profile = findByUserId(userId);
        profile.setBio(dto.bio());
        return mapper.toResponseDTO(professionalProfileRepository.save(profile));
    }

    private ProfessionalProfile findByUserId(String userId) {
        return professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", userId));
    }
}
