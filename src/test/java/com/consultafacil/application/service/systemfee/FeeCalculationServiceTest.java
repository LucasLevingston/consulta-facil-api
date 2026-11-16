package com.consultafacil.application.service.systemfee;

import com.consultafacil.api.dto.fees.FeeCalculationResponseDTO;
import com.consultafacil.api.dto.fees.FeeConfigDTO;
import com.consultafacil.api.dto.fees.PaymentMethodBreakdownDTO;
import com.consultafacil.core.config.TaxConfig;
import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.enums.PaymentMethod;
import com.consultafacil.domain.port.out.plan.PlanRepositoryPort;
import com.consultafacil.domain.port.out.subscription.SubscriptionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeCalculationServiceTest {

    @Mock SubscriptionRepositoryPort subscriptionRepository;
    @Mock PlanRepositoryPort planRepository;

    FeeCalculationService service;

    @BeforeEach
    void setUp() {
        // Default processing fees: credit card 4.98%, pix 0.99%, debit 1.99%
        service = new FeeCalculationService(new TaxConfig(), subscriptionRepository, planRepository);
    }

    private void userHasPlan(String userId, BigDecimal consultationFeeRate) {
        when(subscriptionRepository.findByUserId(userId))
                .thenReturn(Optional.of(Subscription.builder().planId("plan-1").build()));
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(Plan.builder()
                .id("plan-1")
                .slug("profissional")
                .name("Profissional")
                .consultationFeeRate(consultationFeeRate)
                .build()));
    }

    @Test
    void calculate_whenProfessionalAbsorbsFees_deductsFeesFromTheAmount() {
        when(subscriptionRepository.findByUserId("u1")).thenReturn(Optional.empty());

        FeeCalculationResponseDTO result = service.calculate(new BigDecimal("100.00"), PaymentMethod.PIX, true, "u1");

        assertThat(result.mpFeeRate()).isEqualByComparingTo("0.0099");
        assertThat(result.mpFeeAmount()).isEqualByComparingTo("0.99");
        assertThat(result.platformFeeRate()).isEqualByComparingTo("0.05");
        assertThat(result.platformFeeAmount()).isEqualByComparingTo("5.00");
        assertThat(result.totalFees()).isEqualByComparingTo("5.99");
        assertThat(result.patientPays()).isEqualByComparingTo("100.00");
        assertThat(result.professionalReceives()).isEqualByComparingTo("94.01");
        assertThat(result.paymentMethod()).isEqualTo("PIX");
    }

    @Test
    void calculate_whenPatientPaysFees_grossesUpSoProfessionalReceivesTheAmount() {
        userHasPlan("u2", new BigDecimal("0.0200"));

        FeeCalculationResponseDTO result = service.calculate(new BigDecimal("100.00"), PaymentMethod.CREDIT_CARD, false, "u2");

        // gross = 100 / (1 - 0.0498 - 0.02) = 107.50
        assertThat(result.patientPays()).isEqualByComparingTo("107.50");
        assertThat(result.mpFeeAmount()).isEqualByComparingTo("5.35");
        assertThat(result.platformFeeRate()).isEqualByComparingTo("0.02");
        assertThat(result.platformFeeAmount()).isEqualByComparingTo("2.15");
        assertThat(result.totalFees()).isEqualByComparingTo("7.50");
        assertThat(result.professionalReceives()).isEqualByComparingTo("100.00");
        assertThat(result.amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void calculate_usesCreditCardRateForMercadoPagoAndZeroForCash() {
        when(subscriptionRepository.findByUserId("u1")).thenReturn(Optional.empty());

        FeeCalculationResponseDTO mp = service.calculate(new BigDecimal("50.00"), PaymentMethod.MERCADOPAGO, true, "u1");
        FeeCalculationResponseDTO cash = service.calculate(new BigDecimal("50.00"), PaymentMethod.CASH, true, "u1");

        assertThat(mp.mpFeeRate()).isEqualByComparingTo("0.0498");
        assertThat(cash.mpFeeRate()).isEqualByComparingTo("0");
        assertThat(cash.mpFeeAmount()).isEqualByComparingTo("0.00");
        assertThat(cash.professionalReceives()).isEqualByComparingTo("47.50");
    }

    @Test
    void calculate_buildsComparisonForEveryPaymentMethod() {
        when(subscriptionRepository.findByUserId("u1")).thenReturn(Optional.empty());

        FeeCalculationResponseDTO result = service.calculate(new BigDecimal("100.00"), PaymentMethod.DEBIT_CARD, true, "u1");

        assertThat(result.comparison()).extracting(PaymentMethodBreakdownDTO::paymentMethod)
                .containsExactly("MERCADOPAGO", "PIX", "CREDIT_CARD", "DEBIT_CARD", "CASH");
        PaymentMethodBreakdownDTO debit = result.comparison().get(3);
        assertThat(debit.mpFeeAmount()).isEqualByComparingTo(result.mpFeeAmount());
        assertThat(debit.professionalReceives()).isEqualByComparingTo(result.professionalReceives());
        PaymentMethodBreakdownDTO cash = result.comparison().get(4);
        assertThat(cash.totalFees()).isEqualByComparingTo("5.00");
        assertThat(cash.patientPays()).isEqualByComparingTo("100.00");
    }

    @Test
    void calculate_comparisonGrossesUpEachMethodWhenPatientPaysFees() {
        when(subscriptionRepository.findByUserId("u1")).thenReturn(Optional.empty());

        FeeCalculationResponseDTO result = service.calculate(new BigDecimal("100.00"), PaymentMethod.PIX, false, "u1");

        PaymentMethodBreakdownDTO cash = result.comparison().get(4);
        // 100 / (1 - 0 - 0.05) = 105.26
        assertThat(cash.patientPays()).isEqualByComparingTo("105.26");
        assertThat(cash.professionalReceives()).isEqualByComparingTo("100.00");
        assertThat(result.comparison()).allSatisfy(row ->
                assertThat(row.professionalReceives()).isEqualByComparingTo("100.00"));
    }

    @Test
    void calculate_fallsBackToDefaultPlatformRateWhenPlanHasNoRate() {
        userHasPlan("u3", null);

        FeeCalculationResponseDTO result = service.calculate(new BigDecimal("10.00"), PaymentMethod.PIX, true, "u3");

        assertThat(result.platformFeeRate()).isEqualByComparingTo("0.05");
    }

    @Test
    void getConfig_returnsFreePlanDefaultsWhenUserHasNoSubscription() {
        when(subscriptionRepository.findByUserId("u1")).thenReturn(Optional.empty());

        FeeConfigDTO config = service.getConfig("u1");

        assertThat(config.pixFeeRate()).isEqualByComparingTo("0.0099");
        assertThat(config.creditCardFeeRate()).isEqualByComparingTo("0.0498");
        assertThat(config.debitFeeRate()).isEqualByComparingTo("0.0199");
        assertThat(config.platformFeeRate()).isEqualByComparingTo("0.05");
        assertThat(config.planSlug()).isEqualTo("free");
        assertThat(config.planName()).isEqualTo("Free");
    }

    @Test
    void getConfig_usesActivePlanRateAndIdentity() {
        userHasPlan("u2", new BigDecimal("0.0150"));

        FeeConfigDTO config = service.getConfig("u2");

        assertThat(config.platformFeeRate()).isEqualByComparingTo("0.015");
        assertThat(config.planSlug()).isEqualTo("profissional");
        assertThat(config.planName()).isEqualTo("Profissional");
    }

    @Test
    void getConfig_treatsSubscriptionWithMissingPlanAsFree() {
        when(subscriptionRepository.findByUserId("u4"))
                .thenReturn(Optional.of(Subscription.builder().planId("gone").build()));
        when(planRepository.findById("gone")).thenReturn(Optional.empty());

        FeeConfigDTO config = service.getConfig("u4");

        assertThat(config.planSlug()).isEqualTo("free");
        assertThat(config.platformFeeRate()).isEqualByComparingTo("0.05");
    }
}
