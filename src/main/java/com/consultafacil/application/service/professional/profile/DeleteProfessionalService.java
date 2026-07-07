package com.consultafacil.application.service.professional.profile;

import com.consultafacil.application.port.in.DeleteProfessionalUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteProfessionalService implements DeleteProfessionalUseCase {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final UserRepositoryPort userRepository;

    @Override
    @Caching(evict = {
        @CacheEvict(value = "professional-profile", key = "#professionalId"),
        @CacheEvict(value = "professional-services", allEntries = true)
    })
    @Transactional
    public void execute(String professionalId) {
        ProfessionalProfile profile = findById(professionalId);
        profile.getUser().promote(UserRole.PATIENT);
        userRepository.save(profile.getUser());
        professionalProfileRepository.delete(profile);
    }

    private ProfessionalProfile findById(String professionalId) {
        return professionalProfileRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalId));
    }
}
