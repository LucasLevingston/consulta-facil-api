package com.consultafacil.application.service.plan;

import com.consultafacil.api.dto.plan.PlanResponseDTO;
import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.enums.BillingPeriod;
import com.consultafacil.domain.enums.PlanStatus;
import com.consultafacil.domain.port.out.PlanRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListActivePlansServiceTest {

    @Mock PlanRepositoryPort planRepository;
    @Mock PlanMapper mapper;

    @InjectMocks ListActivePlansService service;

    @Test
    void execute_returnsActivePlansOrdered() {
        Plan monthlyPlan = Plan.builder()
                .id("plan-pro-m").slug("monthly").name("Pro Mensal")
                .description("Plano mensal").tier("PRO")
                .billingPeriod(BillingPeriod.MONTHLY)
                .price(new BigDecimal("149.90"))
                .frequency(1).frequencyType("months")
                .features("Agenda online,Prontuário eletrônico,Pacientes ilimitados")
                .status(PlanStatus.ACTIVE).displayOrder(4)
                .build();
        PlanResponseDTO dto = PlanResponseDTO.builder().slug("monthly").build();

        when(planRepository.findAllActiveOrderByDisplayOrder()).thenReturn(List.of(monthlyPlan));
        when(mapper.toDTO(monthlyPlan)).thenReturn(dto);

        List<PlanResponseDTO> result = service.execute();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlug()).isEqualTo("monthly");
    }
}
