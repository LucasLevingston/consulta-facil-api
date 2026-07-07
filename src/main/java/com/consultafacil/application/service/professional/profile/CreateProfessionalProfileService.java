package com.consultafacil.application.service.professional.profile;

import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.application.port.in.professional.profile.CreateProfessionalProfileUseCase;
import com.consultafacil.core.exception.DuplicateResourceException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateProfessionalProfileService implements CreateProfessionalProfileUseCase {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final UserRepositoryPort userRepository;
    private final ProfessionalProfileMapper mapper;

    @Override
    @Transactional
    public ProfessionalResponseDTO execute(String userId, CreateProfessionalDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (professionalProfileRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new DuplicateResourceException("Professional", "license number", dto.getLicenseNumber());
        }

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(user)
                .profession(dto.getProfession())
                .specialty(dto.getSpecialty())
                .licenseNumber(dto.getLicenseNumber())
                .status(ProfessionalProfileStatus.PENDING_REVIEW)
                .build();

        return mapper.toResponseDTO(professionalProfileRepository.save(profile));
    }
}
