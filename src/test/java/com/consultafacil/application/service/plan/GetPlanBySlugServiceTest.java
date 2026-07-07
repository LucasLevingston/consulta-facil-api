package com.consultafacil.application.service.plan;

import com.consultafacil.api.dto.plan.PlanResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.enums.BillingPeriod;
import com.consultafacil.domain.enums.PlanStatus;
import com.consultafacil.domain.port.out.PlanRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPlanBySlugServiceTest {

    @Mock PlanRepositoryPort planRepository;

    GetPlanBySlugService service;

    @BeforeEach
    void setUp() {
        service = new GetPlanBySlugService(planRepository, new PlanMapper());
    }

    private Plan monthlyPlan() {
        return Plan.builder()
                .id("plan-pro-m").slug("monthly").name("Pro Mensal")
                .description("Plano mensal").tier("PRO")
                .billingPeriod(BillingPeriod.MONTHLY)
                .price(new BigDecimal("149.90"))
                .frequency(1).frequencyType("months")
                .features("Agenda online,Prontuário eletrônico,Pacientes ilimitados")
                .status(PlanStatus.ACTIVE).displayOrder(4)
                .build();
    }

    @Test
    void execute_found_returnsDTO() {
        when(planRepository.findBySlug("monthly")).thenReturn(Optional.of(monthlyPlan()));

        PlanResponseDTO dto = service.execute("monthly");

        assertThat(dto.getName()).isEqualTo("Pro Mensal");
        assertThat(dto.getPrice()).isEqualByComparingTo("149.90");
    }

    @Test
    void execute_notFound_throwsResourceNotFoundException() {
        when(planRepository.findBySlug("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("ghost"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_featuresNull_returnsEmptyList() {
        Plan plan = monthlyPlan();
        plan.setFeatures(null);
        when(planRepository.findBySlug("monthly")).thenReturn(Optional.of(plan));

        PlanResponseDTO dto = service.execute("monthly");

        assertThat(dto.getFeatures()).isEmpty();
    }
}
