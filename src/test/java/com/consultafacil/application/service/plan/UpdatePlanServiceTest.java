package com.consultafacil.application.service.plan;

import com.consultafacil.api.dto.plan.PlanResponseDTO;
import com.consultafacil.api.dto.plan.UpdatePlanDTO;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdatePlanServiceTest {

    @Mock PlanRepositoryPort planRepository;

    UpdatePlanService service;

    Plan monthlyPlan;

    @BeforeEach
    void setUp() {
        service = new UpdatePlanService(planRepository, new PlanMapper());
        monthlyPlan = Plan.builder()
                .id("plan-pro-m").slug("monthly").name("Pro Mensal")
                .description("Plano mensal").tier("PRO")
                .billingPeriod(BillingPeriod.MONTHLY)
                .price(new BigDecimal("149.90"))
                .frequency(1).frequencyType("months")
                .features("Agenda online,Prontuário eletrônico,Pacientes ilimitados")
                .status(PlanStatus.ACTIVE).displayOrder(4)
                .build();
        when(planRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void execute_notFound_throwsResourceNotFoundException() {
        when(planRepository.findById("bad")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.execute("bad", new UpdatePlanDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_updatesNameAndPrice() {
        when(planRepository.findById("plan-pro-m")).thenReturn(Optional.of(monthlyPlan));
        UpdatePlanDTO dto = new UpdatePlanDTO();
        dto.setName("Pro Mensal v2");
        dto.setPrice(new BigDecimal("159.90"));

        PlanResponseDTO result = service.execute("plan-pro-m", dto);

        assertThat(result.getName()).isEqualTo("Pro Mensal v2");
        assertThat(result.getPrice()).isEqualByComparingTo("159.90");
    }

    @Test
    void execute_deactivate_setsStatusInactive() {
        when(planRepository.findById("plan-pro-m")).thenReturn(Optional.of(monthlyPlan));
        UpdatePlanDTO dto = new UpdatePlanDTO();
        dto.setStatus(PlanStatus.INACTIVE);

        PlanResponseDTO result = service.execute("plan-pro-m", dto);

        assertThat(result.getStatus()).isEqualTo(PlanStatus.INACTIVE);
    }

    @Test
    void execute_updatesFeatures() {
        when(planRepository.findById("plan-pro-m")).thenReturn(Optional.of(monthlyPlan));
        UpdatePlanDTO dto = new UpdatePlanDTO();
        dto.setFeatures(List.of("X", "Y"));

        PlanResponseDTO result = service.execute("plan-pro-m", dto);

        assertThat(result.getFeatures()).containsExactly("X", "Y");
    }
}
