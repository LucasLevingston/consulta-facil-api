package com.consultafacil.application.service.billing;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileMapper;

import com.consultafacil.api.dto.professional.UpdatePaymentSettingsDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.*;
import com.consultafacil.domain.enums.*;
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

import java.util.Optional;
import java.util.Set;
import com.consultafacil.domain.enums.Specialty;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.consultafacil.domain.enums.Specialty;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdatePaymentSettingsServiceTest {

    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;
    @Mock ProfessionalProfileMapper mapper;
    @InjectMocks UpdatePaymentSettingsService service;

    ProfessionalProfile professional;

    @BeforeEach
    void setUp() {
        User user = User.builder().id("u-1").email("d@e.com").name("Dra.Ana").password("x").role(UserRole.PROFESSIONAL).build();
        professional = new ProfessionalProfile();
        professional.setId("pr-1"); professional.setUser(user); professional.setSpecialty(Specialty.CARDIOLOGIA);

        when(professionalProfileRepository.save(any())).thenReturn(professional);
        when(mapper.toResponseDTO(any())).thenReturn(null);
    }

    @Test void execute_validSettings_savesAndReturns() {
        when(professionalProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(professional));
        UpdatePaymentSettingsDTO dto = UpdatePaymentSettingsDTO.builder()
                .paymentTiming(PaymentTiming.AT_SCHEDULING)
                .acceptedPaymentMethods(Set.of(PaymentMethod.MERCADOPAGO))
                .build();

        service.execute("u-1", dto);

        verify(professionalProfileRepository).save(professional);
    }

    @Test void execute_professionalNotFound_throwsResourceNotFound() {
        when(professionalProfileRepository.findByUserId("bad")).thenReturn(Optional.empty());
        UpdatePaymentSettingsDTO dto = UpdatePaymentSettingsDTO.builder()
                .paymentTiming(PaymentTiming.AT_CONSULTATION)
                .acceptedPaymentMethods(Set.of(PaymentMethod.MERCADOPAGO))
                .build();

        assertThatThrownBy(() -> service.execute("bad", dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
