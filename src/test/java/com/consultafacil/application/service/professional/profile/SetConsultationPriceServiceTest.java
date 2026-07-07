package com.consultafacil.application.service.professional.profile;

import com.consultafacil.application.port.in.professional.profile.GetProfessionalByUserIdUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.*;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.consultafacil.domain.enums.Specialty;

import java.math.BigDecimal;
import java.util.Optional;
import com.consultafacil.domain.enums.Specialty;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.consultafacil.domain.enums.Specialty;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SetConsultationPriceServiceTest {

    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;
    @Mock GetProfessionalByUserIdUseCase getProfessionalByUserId;

    @InjectMocks SetConsultationPriceService service;

    ProfessionalProfile professional;

    @BeforeEach
    void setUp() {
        User user = User.builder().id("u-1").email("d@e.com").name("Dr.").password("x").role(UserRole.PROFESSIONAL).build();
        professional = new ProfessionalProfile();
        professional.setId("pr-1"); professional.setUser(user); professional.setSpecialty(Specialty.CARDIOLOGIA);

        when(professionalProfileRepository.save(any())).thenReturn(professional);
        when(getProfessionalByUserId.execute(any())).thenReturn(null);
    }

    @Test
    void execute_setsConsultationPrice() {
        when(professionalProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(professional));

        service.execute("u-1", new BigDecimal("350.00"));

        verify(professionalProfileRepository).save(professional);
        assertThat(professional.getConsultationPrice()).isEqualByComparingTo("350.00");
    }

    @Test
    void execute_professionalNotFound_throwsNotFound() {
        when(professionalProfileRepository.findByUserId("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("bad", new BigDecimal("250.00")))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
