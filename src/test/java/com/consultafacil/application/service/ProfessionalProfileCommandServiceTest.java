package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.core.exception.DuplicateResourceException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.ProfessionalType;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
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
class ProfessionalProfileCommandServiceTest {

    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;
    @Mock UserRepositoryPort userRepository;

    ProfessionalProfileCommandService service;
    User user;
    ProfessionalProfile profile;

    @BeforeEach
    void setUp() {
        user = User.builder().id("u-1").name("Dra. Ana").email("ana@e.com")
                .password("x").role(UserRole.PROFESSIONAL).build();

        profile = ProfessionalProfile.builder()
                .id("prof-1")
                .user(user)
                .profession(ProfessionalType.MEDICO)
                .specialty(Specialty.CARDIOLOGIA)
                .licenseNumber("CRM-12345")
                .status(ProfessionalProfileStatus.ACTIVE)
                .build();

        service = new ProfessionalProfileCommandService(professionalProfileRepository, userRepository,
                new ProfessionalProfileMapper(new ProfessionalRatingCalculator()));

        when(professionalProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createProfessionalProfile_newLicense_createsProfile() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(professionalProfileRepository.existsByLicenseNumber("CRM-NEW")).thenReturn(false);

        var dto = CreateProfessionalDTO.builder()
                .profession(ProfessionalType.MEDICO).specialty(Specialty.CARDIOLOGIA).licenseNumber("CRM-NEW").build();

        var result = service.createProfessionalProfile("u-1", dto);

        assertThat(result.getSpecialty()).isEqualTo("CARDIOLOGIA");
        verify(professionalProfileRepository).save(any());
    }

    @Test
    void createProfessionalProfile_duplicateLicense_throwsDuplicate() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(professionalProfileRepository.existsByLicenseNumber("CRM-12345")).thenReturn(true);

        var dto = CreateProfessionalDTO.builder()
                .profession(ProfessionalType.MEDICO).specialty(Specialty.CARDIOLOGIA).licenseNumber("CRM-12345").build();

        assertThatThrownBy(() -> service.createProfessionalProfile("u-1", dto))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createProfessionalProfile_userNotFound_throwsNotFound() {
        when(userRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createProfessionalProfile("bad",
                CreateProfessionalDTO.builder().licenseNumber("X").build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void approve_pendingProfile_changesStatusToActive() {
        profile.setStatus(ProfessionalProfileStatus.PENDING_REVIEW);
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(profile));

        service.approveApplication("prof-1");

        assertThat(profile.getStatus()).isEqualTo(ProfessionalProfileStatus.ACTIVE);
        verify(professionalProfileRepository).save(profile);
    }

    @Test
    void reject_pendingProfile_changesStatusToRejected() {
        profile.setStatus(ProfessionalProfileStatus.PENDING_REVIEW);
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(profile));

        service.rejectApplication("prof-1");

        assertThat(profile.getStatus()).isEqualTo(ProfessionalProfileStatus.REJECTED);
    }
}
