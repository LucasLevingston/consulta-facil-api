package com.example.consulta.application.service;

import com.example.consulta.api.dto.professional.CreateProfessionalDTO;
import com.example.consulta.core.exception.DuplicateResourceException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.ProfessionalProfile;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.ProfessionalProfileStatus;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.port.out.ProfessionalProfileRepositoryPort;
import com.example.consulta.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProfessionalServiceTest {

    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;
    @Mock UserRepositoryPort userRepository;

    @InjectMocks ProfessionalService service;

    User user;
    ProfessionalProfile profile;

    @BeforeEach
    void setUp() {
        user = User.builder().id("u-1").name("Dra. Ana").email("ana@e.com")
                .password("x").role(UserRole.PROFESSIONAL).build();

        profile = ProfessionalProfile.builder()
                .id("prof-1")
                .user(user)
                .profession("Médica")
                .specialty("Cardiologia")
                .licenseNumber("CRM-12345")
                .status(ProfessionalProfileStatus.ACTIVE)
                .build();

        when(professionalProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createProfessionalProfile_newLicense_createsProfile() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(professionalProfileRepository.existsByLicenseNumber("CRM-NEW")).thenReturn(false);

        var dto = CreateProfessionalDTO.builder()
                .profession("Médica").specialty("Cardiologia").licenseNumber("CRM-NEW").build();

        var result = service.createProfessionalProfile("u-1", dto);

        assertThat(result.getSpecialty()).isEqualTo("Cardiologia");
        verify(professionalProfileRepository).save(any());
    }

    @Test
    void createProfessionalProfile_duplicateLicense_throwsDuplicate() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(professionalProfileRepository.existsByLicenseNumber("CRM-12345")).thenReturn(true);

        var dto = CreateProfessionalDTO.builder()
                .profession("Médica").specialty("Cardiologia").licenseNumber("CRM-12345").build();

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
    void getProfessionalById_found_returnsDTO() {
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(profile));

        var result = service.getProfessionalById("prof-1");

        assertThat(result.getId()).isEqualTo("prof-1");
        assertThat(result.getName()).isEqualTo("Dra. Ana");
    }

    @Test
    void getProfessionalById_notFound_throwsNotFound() {
        when(professionalProfileRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfessionalById("bad"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getProfessionalByUserId_found_returnsDTO() {
        when(professionalProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(profile));

        var result = service.getProfessionalByUserId("u-1");

        assertThat(result.getUserId()).isEqualTo("u-1");
    }

    @Test
    void getProfessionalByUserId_notFound_throwsNotFound() {
        when(professionalProfileRepository.findByUserId("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfessionalByUserId("bad"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllProfessionals_delegatesWithNormalizedParams() {
        var pageable = PageRequest.of(0, 10);
        when(professionalProfileRepository.findActiveWithFilters("", "cardio", "", pageable))
                .thenReturn(new PageImpl<>(List.of(profile)));

        var result = service.getAllProfessionals(null, "cardio", null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
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
