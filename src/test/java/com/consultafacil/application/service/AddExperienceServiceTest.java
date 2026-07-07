package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.ProfessionalExperienceDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.domain.entity.ProfessionalExperience;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.ProfessionalExperienceRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddExperienceServiceTest {

    @Mock ProfessionalProfileByUserIdFinder profileByUserIdFinder;
    @Mock ProfessionalProfileByIdFinder profileByIdFinder;
    @Mock ProfessionalExperienceRepositoryPort experienceRepository;
    @Mock ProfessionalProfileMapper mapper;

    @InjectMocks AddExperienceService service;

    private ProfessionalProfile profile(String profileId, String userId) {
        User user = User.builder().id(userId).build();
        return ProfessionalProfile.builder()
                .id(profileId).user(user)
                .education(new ArrayList<>())
                .experience(new ArrayList<>())
                .certificates(new ArrayList<>())
                .build();
    }

    @Test
    void addExperience_valid_savesEntry() {
        ProfessionalProfile p = profile("prof-1", "user-1");
        when(profileByUserIdFinder.findOrThrow("user-1")).thenReturn(p);
        when(profileByIdFinder.findOrThrow("prof-1")).thenReturn(p);
        when(experienceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ProfessionalResponseDTO dto = ProfessionalResponseDTO.builder().id(p.getId()).build();
        when(mapper.toResponseDTO(p)).thenReturn(dto);

        ProfessionalExperienceDTO input = new ProfessionalExperienceDTO(null, "Cardiologista", "Hospital das Clínicas", 2015, 2020, null);
        service.execute("user-1", input);

        verify(experienceRepository).save(any(ProfessionalExperience.class));
    }
}
