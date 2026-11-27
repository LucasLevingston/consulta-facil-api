package com.consultafacil.application.service.insurance;

import com.consultafacil.api.dto.insurance.PatientInsuranceDTO;
import com.consultafacil.api.dto.insurance.RegisterPatientInsuranceRequest;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.HealthInsurancePlan;
import com.consultafacil.domain.entity.PatientInsurance;
import com.consultafacil.domain.port.out.insurance.HealthInsurancePlanRepositoryPort;
import com.consultafacil.domain.port.out.insurance.PatientInsuranceRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterPatientInsuranceServiceTest {

    @Mock
    PatientInsuranceRepositoryPort patientInsuranceRepository;
    @Mock
    HealthInsurancePlanRepositoryPort planRepository;

    RegisterPatientInsuranceService service;

    @BeforeEach
    void setUp() {
        service = new RegisterPatientInsuranceService(patientInsuranceRepository, planRepository);
    }

    @Test
    void execute_savesAndReturnsDTO() {
        HealthInsurancePlan plan = HealthInsurancePlan.builder()
                .id("plan-1").name("Amil").code("AMIL").active(true).build();

        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));
        when(patientInsuranceRepository.save(any())).thenAnswer(inv -> {
            PatientInsurance entity = inv.getArgument(0);
            return PatientInsurance.builder()
                    .id("ins-1")
                    .patientId(entity.getPatientId())
                    .plan(entity.getPlan())
                    .memberNumber(entity.getMemberNumber())
                    .holderName(entity.getHolderName())
                    .validUntil(entity.getValidUntil())
                    .build();
        });

        RegisterPatientInsuranceRequest request = new RegisterPatientInsuranceRequest(
                "plan-1", "12345", "João da Silva", LocalDate.of(2026, 12, 31));

        PatientInsuranceDTO result = service.execute("patient-1", request);

        assertThat(result.id()).isEqualTo("ins-1");
        assertThat(result.memberNumber()).isEqualTo("12345");
        assertThat(result.holderName()).isEqualTo("João da Silva");
        assertThat(result.plan().code()).isEqualTo("AMIL");
        assertThat(result.validUntil()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void execute_planNotFound_throwsResourceNotFoundException() {
        when(planRepository.findById("unknown")).thenReturn(Optional.empty());

        RegisterPatientInsuranceRequest request = new RegisterPatientInsuranceRequest(
                "unknown", "12345", "João da Silva", null);

        assertThatThrownBy(() -> service.execute("patient-1", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
