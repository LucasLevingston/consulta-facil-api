package com.example.consulta.application.service;

import com.example.consulta.api.dto.professional.UpdatePaymentSettingsDTO;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.*;
import com.example.consulta.domain.enums.*;
import com.example.consulta.domain.port.out.ProfessionalProfileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdatePaymentSettingsServiceTest {

    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;
    @Mock ProfessionalService professionalService;
    @InjectMocks UpdatePaymentSettingsService service;

    ProfessionalProfile professional;

    @BeforeEach
    void setUp() {
        User user = User.builder().id("u-1").email("d@e.com").name("Dra.Ana").password("x").role(UserRole.PROFESSIONAL).build();
        professional = new ProfessionalProfile();
        professional.setId("pr-1"); professional.setUser(user); professional.setSpecialty("Cardio");

        when(professionalProfileRepository.save(any())).thenReturn(professional);
        when(professionalService.toResponseDTO(any())).thenReturn(null);
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
