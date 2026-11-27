package com.consultafacil.application.service.insurance;

import com.consultafacil.api.dto.insurance.HealthInsurancePlanDTO;
import com.consultafacil.domain.entity.HealthInsurancePlan;
import com.consultafacil.domain.port.out.insurance.HealthInsurancePlanRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListInsurancePlansServiceTest {

    @Mock
    HealthInsurancePlanRepositoryPort planRepository;

    ListInsurancePlansService service;

    @BeforeEach
    void setUp() {
        service = new ListInsurancePlansService(planRepository);
    }

    @Test
    void execute_returnsActivePlansOnly() {
        HealthInsurancePlan plan1 = HealthInsurancePlan.builder()
                .id("plan-1").name("Amil").code("AMIL").active(true).build();
        HealthInsurancePlan plan2 = HealthInsurancePlan.builder()
                .id("plan-2").name("Unimed Nacional").code("UNIMED").active(true).build();

        when(planRepository.findAllActive()).thenReturn(List.of(plan1, plan2));

        List<HealthInsurancePlanDTO> result = service.execute();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).code()).isEqualTo("AMIL");
        assertThat(result.get(1).code()).isEqualTo("UNIMED");
        assertThat(result).allMatch(HealthInsurancePlanDTO::active);
    }

    @Test
    void execute_returnsEmptyList_whenNoPlans() {
        when(planRepository.findAllActive()).thenReturn(List.of());

        List<HealthInsurancePlanDTO> result = service.execute();

        assertThat(result).isEmpty();
    }
}
