package com.consultafacil.application.service.professional.profile;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateBioDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.ProfessionalType;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateBioServiceTest {

    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;
    @Mock ProfessionalProfileMapper mapper;

    UpdateBioService service;

    ProfessionalProfile profile;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id("u-1").name("Dr. Pedro").email("pedro@email.com").build();
        profile = ProfessionalProfile.builder()
                .id("prof-1")
                .user(user)
                .profession(ProfessionalType.MEDICO)
                .specialty(Specialty.NEUROLOGIA)
                .licenseNumber("CRM-22222")
                .status(ProfessionalProfileStatus.ACTIVE)
                .education(new ArrayList<>())
                .experience(new ArrayList<>())
                .certificates(new ArrayList<>())
                .clinicMemberships(new ArrayList<>())
                .build();

        service = new UpdateBioService(professionalProfileRepository, mapper);

        when(professionalProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void execute_userNotFound_throwsResourceNotFoundException() {
        when(professionalProfileRepository.findByUserId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("missing", new UpdateBioDTO("some bio")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_validBio_setsBioOnProfile() {
        when(professionalProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(profile));
        ProfessionalResponseDTO responseDTO = ProfessionalResponseDTO.builder().id("prof-1").build();
        when(mapper.toResponseDTO(any())).thenReturn(responseDTO);

        service.execute("u-1", new UpdateBioDTO("Especialista em neurologia com 10 anos de experiencia."));

        assertThat(profile.getBio()).isEqualTo("Especialista em neurologia com 10 anos de experiencia.");
    }

    @Test
    void execute_nullBio_clearsBio() {
        profile.setBio("Old bio");
        when(professionalProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(profile));
        ProfessionalResponseDTO responseDTO = ProfessionalResponseDTO.builder().id("prof-1").build();
        when(mapper.toResponseDTO(any())).thenReturn(responseDTO);

        service.execute("u-1", new UpdateBioDTO(null));

        assertThat(profile.getBio()).isNull();
    }

    @Test
    void execute_validBio_savesProfile() {
        when(professionalProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(profile));
        ProfessionalResponseDTO responseDTO = ProfessionalResponseDTO.builder().id("prof-1").build();
        when(mapper.toResponseDTO(any())).thenReturn(responseDTO);

        service.execute("u-1", new UpdateBioDTO("New bio text"));

        verify(professionalProfileRepository).save(profile);
    }

    @Test
    void execute_returnsMapperResult() {
        when(professionalProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(profile));
        ProfessionalResponseDTO expected = ProfessionalResponseDTO.builder().id("prof-1").bio("Test bio").build();
        when(mapper.toResponseDTO(any())).thenReturn(expected);

        ProfessionalResponseDTO result = service.execute("u-1", new UpdateBioDTO("Test bio"));

        assertThat(result).isSameAs(expected);
    }
}
