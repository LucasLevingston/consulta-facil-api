package com.consultafacil.application.service.plan;

import com.consultafacil.api.dto.plan.PlanResponseDTO;
import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.enums.BillingPeriod;
import com.consultafacil.domain.enums.PlanStatus;
import com.consultafacil.domain.port.out.plan.PlanRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAllPlansServiceTest {

    @Mock PlanRepositoryPort planRepository;
    @Mock PlanMapper mapper;

    @InjectMocks ListAllPlansService service;

    @Test
    void execute_returnsAll() {
        Plan monthlyPlan = Plan.builder().id("plan-pro-m").slug("monthly").name("Pro Mensal")
                .tier("PRO").billingPeriod(BillingPeriod.MONTHLY).price(new BigDecimal("149.90"))
                .frequency(1).frequencyType("months").status(PlanStatus.ACTIVE).displayOrder(4).build();
        Plan inactive = Plan.builder().id("x").slug("old").name("Old").tier("PRO")
                .billingPeriod(BillingPeriod.ANNUAL).price(BigDecimal.TEN)
                .frequency(12).frequencyType("months").status(PlanStatus.INACTIVE).displayOrder(99).build();

        when(planRepository.findAll()).thenReturn(List.of(monthlyPlan, inactive));
        when(mapper.toDTO(any())).thenReturn(PlanResponseDTO.builder().build());

        List<PlanResponseDTO> result = service.execute();

        assertThat(result).hasSize(2);
    }
}
