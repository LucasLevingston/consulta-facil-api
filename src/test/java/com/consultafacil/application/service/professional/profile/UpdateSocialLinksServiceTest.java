package com.consultafacil.application.service.professional.profile;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateSocialLinksDTO;
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
class UpdateSocialLinksServiceTest {

    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;
    @Mock ProfessionalProfileMapper mapper;

    UpdateSocialLinksService service;

    ProfessionalProfile profile;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id("u-1").name("Dr. Julia").email("julia@email.com").build();
        profile = ProfessionalProfile.builder()
                .id("prof-1")
                .user(user)
                .profession(ProfessionalType.MEDICO)
                .specialty(Specialty.CARDIOLOGIA)
                .licenseNumber("CRM-11111")
                .status(ProfessionalProfileStatus.ACTIVE)
                .education(new ArrayList<>())
                .experience(new ArrayList<>())
                .certificates(new ArrayList<>())
                .clinicMemberships(new ArrayList<>())
                .build();

        service = new UpdateSocialLinksService(professionalProfileRepository, mapper);

        when(professionalProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void execute_userNotFound_throwsResourceNotFoundException() {
        when(professionalProfileRepository.findByUserId("missing")).thenReturn(Optional.empty());

        UpdateSocialLinksDTO dto = new UpdateSocialLinksDTO(null, null, null, null);

        assertThatThrownBy(() -> service.execute("missing", dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_validLinks_setsAllSocialFields() {
        when(professionalProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(profile));
        ProfessionalResponseDTO responseDTO = ProfessionalResponseDTO.builder().id("prof-1").build();
        when(mapper.toResponseDTO(any())).thenReturn(responseDTO);

        UpdateSocialLinksDTO dto = new UpdateSocialLinksDTO(
                "https://instagram.com/julia",
                "https://linkedin.com/in/julia",
                "https://juliamed.com.br",
                "https://facebook.com/julia"
        );

        service.execute("u-1", dto);

        assertThat(profile.getInstagramUrl()).isEqualTo("https://instagram.com/julia");
        assertThat(profile.getLinkedinUrl()).isEqualTo("https://linkedin.com/in/julia");
        assertThat(profile.getWebsiteUrl()).isEqualTo("https://juliamed.com.br");
        assertThat(profile.getFacebookUrl()).isEqualTo("https://facebook.com/julia");
    }

    @Test
    void execute_nullLinks_clearsSocialFields() {
        profile.setInstagramUrl("https://instagram.com/old");
        profile.setLinkedinUrl("https://linkedin.com/in/old");
        when(professionalProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(profile));
        ProfessionalResponseDTO responseDTO = ProfessionalResponseDTO.builder().id("prof-1").build();
        when(mapper.toResponseDTO(any())).thenReturn(responseDTO);

        UpdateSocialLinksDTO dto = new UpdateSocialLinksDTO(null, null, null, null);

        service.execute("u-1", dto);

        assertThat(profile.getInstagramUrl()).isNull();
        assertThat(profile.getLinkedinUrl()).isNull();
    }

    @Test
    void execute_validLinks_savesProfile() {
        when(professionalProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(profile));
        ProfessionalResponseDTO responseDTO = ProfessionalResponseDTO.builder().id("prof-1").build();
        when(mapper.toResponseDTO(any())).thenReturn(responseDTO);

        UpdateSocialLinksDTO dto = new UpdateSocialLinksDTO(
                "https://instagram.com/julia", null, null, null);

        service.execute("u-1", dto);

        verify(professionalProfileRepository).save(profile);
    }

    @Test
    void execute_returnsMapperResult() {
        when(professionalProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(profile));
        ProfessionalResponseDTO expected = ProfessionalResponseDTO.builder().id("prof-1").build();
        when(mapper.toResponseDTO(any())).thenReturn(expected);

        UpdateSocialLinksDTO dto = new UpdateSocialLinksDTO(null, null, null, null);

        ProfessionalResponseDTO result = service.execute("u-1", dto);

        assertThat(result).isSameAs(expected);
    }
}
