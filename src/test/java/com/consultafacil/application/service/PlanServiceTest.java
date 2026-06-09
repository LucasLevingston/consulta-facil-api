package com.consultafacil.application.service;

import com.consultafacil.api.dto.plan.CreatePlanDTO;
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
import org.mockito.InjectMocks;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlanServiceTest {

    @Mock PlanRepositoryPort planRepository;

    @InjectMocks PlanService service;

    Plan monthlyPlan;

    @BeforeEach
    void setUp() {
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
    void listActivePlans_returnsActivePlansOrdered() {
        when(planRepository.findAllActiveOrderByDisplayOrder()).thenReturn(List.of(monthlyPlan));
        List<PlanResponseDTO> result = service.listActivePlans();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlug()).isEqualTo("monthly");
    }

    @Test
    void listAllPlans_returnsAll() {
        Plan inactive = Plan.builder().id("x").slug("old").name("Old").tier("PRO")
                .billingPeriod(BillingPeriod.ANNUAL).price(BigDecimal.TEN)
                .frequency(12).frequencyType("months").status(PlanStatus.INACTIVE).displayOrder(99).build();
        when(planRepository.findAll()).thenReturn(List.of(monthlyPlan, inactive));
        List<PlanResponseDTO> result = service.listAllPlans();
        assertThat(result).hasSize(2);
    }

    @Test
    void getBySlug_found_returnsDTO() {
        when(planRepository.findBySlug("monthly")).thenReturn(Optional.of(monthlyPlan));
        PlanResponseDTO dto = service.getBySlug("monthly");
        assertThat(dto.getName()).isEqualTo("Pro Mensal");
        assertThat(dto.getPrice()).isEqualByComparingTo("149.90");
    }

    @Test
    void getBySlug_notFound_throwsResourceNotFoundException() {
        when(planRepository.findBySlug("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getBySlug("ghost"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createPlan_savesAndReturnsDTO() {
        CreatePlanDTO dto = new CreatePlanDTO();
        dto.setSlug("pro-anual");
        dto.setName("Pro Anual");
        dto.setDescription("Anual");
        dto.setTier("PRO");
        dto.setBillingPeriod(BillingPeriod.ANNUAL);
        dto.setPrice(new BigDecimal("1349.80"));
        dto.setFrequency(12);
        dto.setFrequencyType("months");
        dto.setFeatures(List.of("Agenda online", "Telemedicina"));
        dto.setDisplayOrder(6);

        PlanResponseDTO result = service.createPlan(dto);

        verify(planRepository).save(any());
        assertThat(result.getSlug()).isEqualTo("pro-anual");
        assertThat(result.getFeatures()).containsExactly("Agenda online", "Telemedicina");
    }

    @Test
    void createPlan_featuresStoredAsCommaSeparated() {
        CreatePlanDTO dto = new CreatePlanDTO();
        dto.setSlug("test"); dto.setName("Test"); dto.setTier("BASIC");
        dto.setBillingPeriod(BillingPeriod.MONTHLY);
        dto.setPrice(BigDecimal.TEN);
        dto.setFrequency(1); dto.setFrequencyType("months");
        dto.setFeatures(List.of("A", "B", "C"));
        dto.setDisplayOrder(1);

        PlanResponseDTO result = service.createPlan(dto);
        assertThat(result.getFeatures()).containsExactly("A", "B", "C");
    }

    @Test
    void createPlan_nullFeatures_returnsEmptyList() {
        CreatePlanDTO dto = new CreatePlanDTO();
        dto.setSlug("nofeature"); dto.setName("NoFeature"); dto.setTier("BASIC");
        dto.setBillingPeriod(BillingPeriod.MONTHLY);
        dto.setPrice(BigDecimal.TEN);
        dto.setFrequency(1); dto.setFrequencyType("months");
        dto.setFeatures(null);
        dto.setDisplayOrder(0);

        PlanResponseDTO result = service.createPlan(dto);
        assertThat(result.getFeatures()).isEmpty();
    }

    @Test
    void updatePlan_notFound_throwsResourceNotFoundException() {
        when(planRepository.findById("bad")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updatePlan("bad", new UpdatePlanDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatePlan_updatesNameAndPrice() {
        when(planRepository.findById("plan-pro-m")).thenReturn(Optional.of(monthlyPlan));
        UpdatePlanDTO dto = new UpdatePlanDTO();
        dto.setName("Pro Mensal v2");
        dto.setPrice(new BigDecimal("159.90"));

        PlanResponseDTO result = service.updatePlan("plan-pro-m", dto);

        assertThat(result.getName()).isEqualTo("Pro Mensal v2");
        assertThat(result.getPrice()).isEqualByComparingTo("159.90");
    }

    @Test
    void updatePlan_deactivate_setsStatusInactive() {
        when(planRepository.findById("plan-pro-m")).thenReturn(Optional.of(monthlyPlan));
        UpdatePlanDTO dto = new UpdatePlanDTO();
        dto.setStatus(PlanStatus.INACTIVE);

        PlanResponseDTO result = service.updatePlan("plan-pro-m", dto);

        assertThat(result.getStatus()).isEqualTo(PlanStatus.INACTIVE);
    }

    @Test
    void updatePlan_updatesFeatures() {
        when(planRepository.findById("plan-pro-m")).thenReturn(Optional.of(monthlyPlan));
        UpdatePlanDTO dto = new UpdatePlanDTO();
        dto.setFeatures(List.of("X", "Y"));

        PlanResponseDTO result = service.updatePlan("plan-pro-m", dto);

        assertThat(result.getFeatures()).containsExactly("X", "Y");
    }

    @Test
    void toDTO_featuresNull_returnsEmptyList() {
        monthlyPlan.setFeatures(null);
        when(planRepository.findBySlug("monthly")).thenReturn(Optional.of(monthlyPlan));
        PlanResponseDTO dto = service.getBySlug("monthly");
        assertThat(dto.getFeatures()).isEmpty();
    }

    @Test
    void plan_durationDays_monthly_returns30() {
        assertThat(monthlyPlan.durationDays()).isEqualTo(30);
    }

    @Test
    void plan_durationDays_annual_returns365() {
        Plan annual = Plan.builder().id("a").slug("yearly").name("Anual").tier("PRO")
                .billingPeriod(BillingPeriod.ANNUAL).price(BigDecimal.TEN)
                .frequency(12).frequencyType("months").status(PlanStatus.ACTIVE).displayOrder(1).build();
        assertThat(annual.durationDays()).isEqualTo(365);
    }

    @Test
    void plan_durationDays_semiannual_returns180() {
        Plan semi = Plan.builder().id("s").slug("semi").name("Semi").tier("PRO")
                .billingPeriod(BillingPeriod.SEMIANNUAL).price(BigDecimal.TEN)
                .frequency(6).frequencyType("months").status(PlanStatus.ACTIVE).displayOrder(2).build();
        assertThat(semi.durationDays()).isEqualTo(180);
    }
}
