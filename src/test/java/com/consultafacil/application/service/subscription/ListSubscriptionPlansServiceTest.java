package com.consultafacil.application.service.subscription;

import com.consultafacil.api.dto.subscription.SubscriptionPlanInfoDTO;
import com.consultafacil.domain.enums.SubscriptionPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListSubscriptionPlansServiceTest {

    ListSubscriptionPlansService service;

    @BeforeEach
    void setUp() {
        service = new ListSubscriptionPlansService();
    }

    @Test
    void execute_returnsThreePlans() {
        List<SubscriptionPlanInfoDTO> plans = service.execute();

        assertThat(plans).hasSize(3);
    }

    @Test
    void execute_containsFreeProfessionalClinicPlans() {
        List<SubscriptionPlanInfoDTO> plans = service.execute();

        assertThat(plans).extracting(SubscriptionPlanInfoDTO::getPlan)
                .containsExactly(SubscriptionPlan.FREE, SubscriptionPlan.PROFESSIONAL, SubscriptionPlan.CLINIC);
    }

    @Test
    void execute_freePlan_hasZeroPrice() {
        List<SubscriptionPlanInfoDTO> plans = service.execute();

        SubscriptionPlanInfoDTO free = plans.stream()
                .filter(p -> p.getPlan() == SubscriptionPlan.FREE)
                .findFirst().orElseThrow();

        assertThat(free.getMonthlyPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(free.getName()).isNotBlank();
        assertThat(free.getFeatures()).isNotEmpty();
        assertThat(free.getMaxAppointmentsPerMonth()).isPositive();
    }

    @Test
    void execute_professionalPlan_hasPriceAndUnlimitedAppointments() {
        List<SubscriptionPlanInfoDTO> plans = service.execute();

        SubscriptionPlanInfoDTO professional = plans.stream()
                .filter(p -> p.getPlan() == SubscriptionPlan.PROFESSIONAL)
                .findFirst().orElseThrow();

        assertThat(professional.getMonthlyPrice()).isGreaterThan(BigDecimal.ZERO);
        assertThat(professional.getName()).isNotBlank();
        assertThat(professional.getFeatures()).isNotEmpty();
        assertThat(professional.getMaxAppointmentsPerMonth()).isEqualTo(-1);
    }

    @Test
    void execute_allPlansHaveNonBlankNamesAndFeatures() {
        List<SubscriptionPlanInfoDTO> plans = service.execute();

        assertThat(plans).allSatisfy(plan -> {
            assertThat(plan.getName()).isNotBlank();
            assertThat(plan.getFeatures()).isNotEmpty();
            assertThat(plan.getPlan()).isNotNull();
            assertThat(plan.getMonthlyPrice()).isNotNull();
        });
    }
}
