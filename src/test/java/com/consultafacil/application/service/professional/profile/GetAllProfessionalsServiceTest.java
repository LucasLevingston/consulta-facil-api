package com.consultafacil.application.service.professional.profile;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetAllProfessionalsServiceTest {

    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;

    GetAllProfessionalsService service;
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
        service = new GetAllProfessionalsService(professionalProfileRepository,
                new ProfessionalProfileSummaryMapper(ratingCalculator, consultationCountCalculator));
    }

    @Test
    void execute_delegatesWithNormalizedParams() {
        var pageable = PageRequest.of(0, 10);
        when(professionalProfileRepository.findActiveWithFilters("", "cardio", "", pageable))
                .thenReturn(new PageImpl<>(List.of(profile)));

        var result = service.execute(null, "cardio", null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
