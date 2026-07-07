package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateCouncilDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.CouncilType;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCouncilServiceTest {

    @Mock ProfessionalProfileByUserIdFinder profileFinder;
    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;
    @Mock ProfessionalProfileMapper mapper;

    @InjectMocks UpdateCouncilService service;

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
    void updateCouncil_validUser_setsCouncilFields() {
        ProfessionalProfile p = profile("prof-1", "user-1");
        when(profileFinder.findOrThrow("user-1")).thenReturn(p);
        when(professionalProfileRepository.save(p)).thenReturn(p);
        ProfessionalResponseDTO dto = ProfessionalResponseDTO.builder().id(p.getId()).build();
        when(mapper.toResponseDTO(p)).thenReturn(dto);

        UpdateCouncilDTO input = new UpdateCouncilDTO(CouncilType.CRM, "SP");
        ProfessionalResponseDTO result = service.execute("user-1", input);

        assertThat(p.getCouncilType()).isEqualTo(CouncilType.CRM);
        assertThat(p.getCouncilState()).isEqualTo("SP");
        assertThat(result.getId()).isEqualTo("prof-1");
    }

    @Test
    void updateCouncil_profileNotFound_throwsNotFound() {
        when(profileFinder.findOrThrow("unknown")).thenThrow(new ResourceNotFoundException("Professional profile", "unknown"));
        assertThatThrownBy(() -> service.execute("unknown", new UpdateCouncilDTO(CouncilType.CRM, "RJ")))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
