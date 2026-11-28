package com.consultafacil.application.service.professional.profile;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.ProfessionalType;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetProfessionalsNearbyServiceTest {

    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;

    GetProfessionalsNearbyService service;
    ProfessionalProfile profile;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id("u-3")
                .name("Dra. Helena")
                .email("helena@email.com")
                .password("hashed")
                .role(UserRole.PROFESSIONAL)
                .build();

        profile = ProfessionalProfile.builder()
                .id("prof-3")
                .user(user)
                .profession(ProfessionalType.MEDICO)
                .specialty(Specialty.CARDIOLOGIA)
                .licenseNumber("CRM-33333")
                .status(ProfessionalProfileStatus.ACTIVE)
                .latitude(-23.5)
                .longitude(-46.6)
                .build();

        ProfessionalRatingCalculator ratingCalculator = new ProfessionalRatingCalculator();
        ProfessionalConsultationCountCalculator consultationCountCalculator = new ProfessionalConsultationCountCalculator();
        service = new GetProfessionalsNearbyService(
                professionalProfileRepository,
                new ProfessionalProfileMapper(ratingCalculator, consultationCountCalculator));
    }

    @Test
    void execute_withResults_returnsMappedDTOs() {
        when(professionalProfileRepository.findNearby(-23.5, -46.6, 10.0, "CARDIOLOGIA", "MEDICO"))
                .thenReturn(List.of(profile));

        List<ProfessionalResponseDTO> result = service.execute(-23.5, -46.6, 10.0, "CARDIOLOGIA", "MEDICO");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("prof-3");
        assertThat(result.get(0).getName()).isEqualTo("Dra. Helena");
    }

    @Test
    void execute_emptyResult_returnsEmptyList() {
        when(professionalProfileRepository.findNearby(-23.5, -46.6, 5.0, "", ""))
                .thenReturn(Collections.emptyList());

        List<ProfessionalResponseDTO> result = service.execute(-23.5, -46.6, 5.0, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void execute_unknownEnumValue_passedThrough() {
        when(professionalProfileRepository.findNearby(-23.5, -46.6, 10.0, "unknown_spec", "unknown_prof"))
                .thenReturn(Collections.emptyList());

        List<ProfessionalResponseDTO> result = service.execute(-23.5, -46.6, 10.0, "unknown_spec", "unknown_prof");

        assertThat(result).isEmpty();
    }

    @Test
    void execute_validEnumNames_areNormalized() {
        when(professionalProfileRepository.findNearby(-23.5, -46.6, 10.0, "CARDIOLOGIA", "MEDICO"))
                .thenReturn(List.of(profile));

        List<ProfessionalResponseDTO> result = service.execute(-23.5, -46.6, 10.0, "cardiologia", "medico");

        assertThat(result).hasSize(1);
    }
}
