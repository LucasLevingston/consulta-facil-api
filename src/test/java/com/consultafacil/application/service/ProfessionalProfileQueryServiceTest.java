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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProfessionalProfileQueryServiceTest {

    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;

    ProfessionalProfileQueryService service;
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
        service = new ProfessionalProfileQueryService(professionalProfileRepository,
                new ProfessionalProfileMapper(ratingCalculator),
                new ProfessionalProfileSummaryMapper(ratingCalculator));
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
}
