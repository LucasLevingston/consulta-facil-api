package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.ProfessionalEducationDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.domain.entity.ProfessionalEducation;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.DegreeType;
import com.consultafacil.domain.port.out.ProfessionalEducationRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddEducationServiceTest {

    @Mock ProfessionalProfileByUserIdFinder profileByUserIdFinder;
    @Mock ProfessionalProfileByIdFinder profileByIdFinder;
    @Mock ProfessionalEducationRepositoryPort educationRepository;
    @Mock ProfessionalProfileMapper mapper;

    @InjectMocks AddEducationService service;

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
    void addEducation_valid_savesAndReturnsProfile() {
        ProfessionalProfile p = profile("prof-1", "user-1");
        when(profileByUserIdFinder.findOrThrow("user-1")).thenReturn(p);
        when(profileByIdFinder.findOrThrow("prof-1")).thenReturn(p);
        when(educationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ProfessionalResponseDTO dto = ProfessionalResponseDTO.builder().id(p.getId()).build();
        when(mapper.toResponseDTO(p)).thenReturn(dto);

        ProfessionalEducationDTO input = new ProfessionalEducationDTO(null, DegreeType.GRADUATION, "USP", "Medicina", 2010);
        ProfessionalResponseDTO result = service.execute("user-1", input);

        verify(educationRepository).save(any(ProfessionalEducation.class));
        assertThat(result.getId()).isEqualTo("prof-1");
    }
}
