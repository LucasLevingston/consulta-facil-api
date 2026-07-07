package com.consultafacil.application.service;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.ProfessionalType;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetProfessionalByIdServiceTest {

    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;

    GetProfessionalByIdService service;
    ProfessionalProfile profile;

    @BeforeEach
    void setUp() {
        User user = User.builder().id("u-1").name("Dra. Ana").email("ana@e.com")
                .password("x").role(UserRole.PROFESSIONAL).build();

        profile = ProfessionalProfile.builder()
                .id("prof-1")
                .user(user)
                .profession(ProfessionalType.MEDICO)
                .specialty(Specialty.CARDIOLOGIA)
                .licenseNumber("CRM-12345")
                .status(ProfessionalProfileStatus.ACTIVE)
                .build();

        ProfessionalRatingCalculator ratingCalculator = new ProfessionalRatingCalculator();
        ProfessionalConsultationCountCalculator consultationCountCalculator = new ProfessionalConsultationCountCalculator();
        service = new GetProfessionalByIdService(professionalProfileRepository,
                new ProfessionalProfileMapper(ratingCalculator, consultationCountCalculator));
    }

    @Test
    void execute_found_returnsDTO() {
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(profile));

        var result = service.execute("prof-1");

        assertThat(result.getId()).isEqualTo("prof-1");
        assertThat(result.getName()).isEqualTo("Dra. Ana");
    }

    @Test
    void execute_notFound_throwsNotFound() {
        when(professionalProfileRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("bad"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
