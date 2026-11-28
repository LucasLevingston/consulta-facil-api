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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetPendingApplicationsServiceTest {

    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;

    GetPendingApplicationsService service;
    ProfessionalProfile pendingProfile;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id("u-2")
                .name("Dr. Fernando")
                .email("fernando@email.com")
                .password("hashed")
                .role(UserRole.PROFESSIONAL)
                .build();

        pendingProfile = ProfessionalProfile.builder()
                .id("prof-2")
                .user(user)
                .profession(ProfessionalType.MEDICO)
                .specialty(Specialty.NEUROLOGIA)
                .licenseNumber("CRM-22222")
                .status(ProfessionalProfileStatus.PENDING_REVIEW)
                .build();

        ProfessionalRatingCalculator ratingCalculator = new ProfessionalRatingCalculator();
        ProfessionalConsultationCountCalculator consultationCountCalculator = new ProfessionalConsultationCountCalculator();
        service = new GetPendingApplicationsService(
                professionalProfileRepository,
                new ProfessionalProfileMapper(ratingCalculator, consultationCountCalculator));
    }

    @Test
    void execute_returnsPendingApplications() {
        Pageable pageable = PageRequest.of(0, 10);
        when(professionalProfileRepository.findByStatus(ProfessionalProfileStatus.PENDING_REVIEW, pageable))
                .thenReturn(new PageImpl<>(List.of(pendingProfile)));

        Page<ProfessionalResponseDTO> result = service.execute(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo("prof-2");
    }

    @Test
    void execute_emptyList_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(professionalProfileRepository.findByStatus(ProfessionalProfileStatus.PENDING_REVIEW, pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<ProfessionalResponseDTO> result = service.execute(pageable);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }
}
