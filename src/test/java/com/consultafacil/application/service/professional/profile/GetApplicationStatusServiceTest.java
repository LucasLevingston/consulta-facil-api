package com.consultafacil.application.service.professional.profile;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetApplicationStatusServiceTest {

    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;

    GetApplicationStatusService service;
    ProfessionalProfile profile;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id("u-1")
                .name("Dra. Beatriz")
                .email("beatriz@email.com")
                .password("hashed")
                .role(UserRole.PROFESSIONAL)
                .build();

        profile = ProfessionalProfile.builder()
                .id("prof-1")
                .user(user)
                .profession(ProfessionalType.MEDICO)
                .specialty(Specialty.CARDIOLOGIA)
                .licenseNumber("CRM-11111")
                .status(ProfessionalProfileStatus.PENDING_REVIEW)
                .build();

        ProfessionalRatingCalculator ratingCalculator = new ProfessionalRatingCalculator();
        ProfessionalConsultationCountCalculator consultationCountCalculator = new ProfessionalConsultationCountCalculator();
        service = new GetApplicationStatusService(
                professionalProfileRepository,
                new ProfessionalProfileMapper(ratingCalculator, consultationCountCalculator));
    }

    @Test
    void execute_existingUser_returnsMappedDTO() {
        when(professionalProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(profile));

        ProfessionalResponseDTO result = service.execute("u-1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("prof-1");
        assertThat(result.getUserId()).isEqualTo("u-1");
        assertThat(result.getName()).isEqualTo("Dra. Beatriz");
    }

    @Test
    void execute_statusReflectedInDTO() {
        when(professionalProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(profile));

        ProfessionalResponseDTO result = service.execute("u-1");

        assertThat(result.getStatus()).isEqualTo(ProfessionalProfileStatus.PENDING_REVIEW);
    }

    @Test
    void execute_userNotFound_throwsResourceNotFoundException() {
        when(professionalProfileRepository.findByUserId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
