package com.consultafacil.application.service.plan;

import com.consultafacil.api.dto.plan.CreatePlanDTO;
import com.consultafacil.api.dto.plan.PlanResponseDTO;
import com.consultafacil.domain.enums.BillingPeriod;
import com.consultafacil.domain.port.out.PlanRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePlanServiceTest {

    @Mock PlanRepositoryPort planRepository;

    CreatePlanService service;

    @BeforeEach
    void setUp() {
        service = new CreatePlanService(planRepository, new PlanMapper());
        when(planRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void execute_savesAndReturnsDTO() {
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

        PlanResponseDTO result = service.execute(dto);

        verify(planRepository).save(any());
        assertThat(result.getSlug()).isEqualTo("pro-anual");
        assertThat(result.getFeatures()).containsExactly("Agenda online", "Telemedicina");
    }

    @Test
    void execute_featuresStoredAsCommaSeparated() {
        CreatePlanDTO dto = new CreatePlanDTO();
        dto.setSlug("test"); dto.setName("Test"); dto.setTier("BASIC");
        dto.setBillingPeriod(BillingPeriod.MONTHLY);
        dto.setPrice(BigDecimal.TEN);
        dto.setFrequency(1); dto.setFrequencyType("months");
        dto.setFeatures(List.of("A", "B", "C"));
        dto.setDisplayOrder(1);

        PlanResponseDTO result = service.execute(dto);
        assertThat(result.getFeatures()).containsExactly("A", "B", "C");
    }

    @Test
    void execute_nullFeatures_returnsEmptyList() {
        CreatePlanDTO dto = new CreatePlanDTO();
        dto.setSlug("nofeature"); dto.setName("NoFeature"); dto.setTier("BASIC");
        dto.setBillingPeriod(BillingPeriod.MONTHLY);
        dto.setPrice(BigDecimal.TEN);
        dto.setFrequency(1); dto.setFrequencyType("months");
        dto.setFeatures(null);
        dto.setDisplayOrder(0);

        PlanResponseDTO result = service.execute(dto);
        assertThat(result.getFeatures()).isEmpty();
    }
}
