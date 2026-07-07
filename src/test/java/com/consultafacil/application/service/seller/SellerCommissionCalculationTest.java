package com.consultafacil.application.service.seller;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

class SellerCommissionCalculationTest {

    @Test
    void commissionAmountCalculation_20percent_of150_is30() {
        BigDecimal gross = new BigDecimal("150.00");
        BigDecimal rate = new BigDecimal("20.00");
        BigDecimal expected = new BigDecimal("30.00");

        BigDecimal commission = gross.multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        assertThat(commission).isEqualByComparingTo(expected);
    }

    @Test
    void commissionAmountCalculation_15percent_roundsCorrectly() {
        BigDecimal gross = new BigDecimal("149.90");
        BigDecimal rate = new BigDecimal("15.00");

        BigDecimal commission = gross.multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        assertThat(commission).isEqualByComparingTo("22.49");
    }
}
