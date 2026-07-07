package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.core.exception.DuplicateResourceException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.User;
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
class CreateProfessionalProfileServiceTest {

    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;
    @Mock UserRepositoryPort userRepository;

    CreateProfessionalProfileService service;
    User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id("u-1").name("Dra. Ana").email("ana@e.com")
                .password("x").role(UserRole.PROFESSIONAL).build();

        ProfessionalRatingCalculator ratingCalculator = new ProfessionalRatingCalculator();
        ProfessionalConsultationCountCalculator consultationCountCalculator = new ProfessionalConsultationCountCalculator();
        service = new CreateProfessionalProfileService(professionalProfileRepository, userRepository,
                new ProfessionalProfileMapper(ratingCalculator, consultationCountCalculator));

        when(professionalProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void execute_newLicense_createsProfile() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(professionalProfileRepository.existsByLicenseNumber("CRM-NEW")).thenReturn(false);

        var dto = CreateProfessionalDTO.builder()
                .profession(ProfessionalType.MEDICO).specialty(Specialty.CARDIOLOGIA).licenseNumber("CRM-NEW").build();

        var result = service.execute("u-1", dto);

        assertThat(result.getSpecialty()).isEqualTo("CARDIOLOGIA");
        verify(professionalProfileRepository).save(any());
    }

    @Test
    void execute_duplicateLicense_throwsDuplicate() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(professionalProfileRepository.existsByLicenseNumber("CRM-12345")).thenReturn(true);

        var dto = CreateProfessionalDTO.builder()
                .profession(ProfessionalType.MEDICO).specialty(Specialty.CARDIOLOGIA).licenseNumber("CRM-12345").build();

        assertThatThrownBy(() -> service.execute("u-1", dto))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void execute_userNotFound_throwsNotFound() {
        when(userRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("bad",
                CreateProfessionalDTO.builder().licenseNumber("X").build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
