package com.consultafacil.application.service.professional.profile;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.ProfessionalType;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeleteProfessionalServiceTest {

    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;
    @Mock UserRepositoryPort userRepository;

    DeleteProfessionalService service;
    ProfessionalProfile profile;
    User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("u-1")
                .name("Dr. Carlos")
                .email("carlos@email.com")
                .password("hashed")
                .role(UserRole.PROFESSIONAL)
                .build();

        profile = ProfessionalProfile.builder()
                .id("prof-1")
                .user(user)
                .profession(ProfessionalType.MEDICO)
                .specialty(Specialty.CARDIOLOGIA)
                .licenseNumber("CRM-99999")
                .status(ProfessionalProfileStatus.ACTIVE)
                .build();

        service = new DeleteProfessionalService(professionalProfileRepository, userRepository);

        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void execute_found_demotesUserToPatient() {
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(profile));

        service.execute("prof-1");

        assertThat(user.getRole()).isEqualTo(UserRole.PATIENT);
    }

    @Test
    void execute_found_savesUser() {
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(profile));

        service.execute("prof-1");

        verify(userRepository).save(user);
    }

    @Test
    void execute_found_deletesProfile() {
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(profile));

        service.execute("prof-1");

        verify(professionalProfileRepository).delete(profile);
    }

    @Test
    void execute_notFound_throwsResourceNotFoundException() {
        when(professionalProfileRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
