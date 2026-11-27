package com.consultafacil.application.service.insurance;

import com.consultafacil.api.dto.insurance.AddProfessionalInsuranceRequest;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.HealthInsurancePlan;
import com.consultafacil.domain.entity.ProfessionalInsuranceAccepted;
import com.consultafacil.domain.port.out.insurance.HealthInsurancePlanRepositoryPort;
import com.consultafacil.domain.port.out.insurance.ProfessionalInsuranceRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddProfessionalInsuranceServiceTest {

    @Mock
    ProfessionalInsuranceRepositoryPort professionalInsuranceRepository;
    @Mock
    HealthInsurancePlanRepositoryPort planRepository;

    AddProfessionalInsuranceService service;

    @BeforeEach
    void setUp() {
        service = new AddProfessionalInsuranceService(professionalInsuranceRepository, planRepository);
    }

    @Test
    void execute_savesAcceptedPlan() {
        HealthInsurancePlan plan = HealthInsurancePlan.builder()
                .id("plan-1").name("Amil").code("AMIL").active(true).build();

        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));
        when(professionalInsuranceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AddProfessionalInsuranceRequest request = new AddProfessionalInsuranceRequest("plan-1", "Consultas gerais");

        service.execute("prof-1", request);

        ArgumentCaptor<ProfessionalInsuranceAccepted> captor = ArgumentCaptor.forClass(ProfessionalInsuranceAccepted.class);
        verify(professionalInsuranceRepository).save(captor.capture());

        ProfessionalInsuranceAccepted saved = captor.getValue();
        assertThat(saved.getProfessionalId()).isEqualTo("prof-1");
        assertThat(saved.getPlanId()).isEqualTo("plan-1");
        assertThat(saved.getNotes()).isEqualTo("Consultas gerais");
    }

    @Test
    void execute_planNotFound_throwsResourceNotFoundException() {
        when(planRepository.findById("unknown")).thenReturn(Optional.empty());

        AddProfessionalInsuranceRequest request = new AddProfessionalInsuranceRequest("unknown", null);

        assertThatThrownBy(() -> service.execute("prof-1", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
