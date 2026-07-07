package com.consultafacil.application.service;

import com.consultafacil.api.dto.tax.TaxBreakdown;
import com.consultafacil.core.config.TaxConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaxBreakdownCalculatorTest {

    @InjectMocks TaxBreakdownCalculator service;

    TaxConfig config;

    @BeforeEach
    void setUp() {
        config = new TaxConfig();
        // defaults: SIMPLES_NACIONAL 6%, ISS 2.9%, CC 4.98%, PIX 0.99%, debit 1.99%
        org.springframework.test.util.ReflectionTestUtils.setField(service, "taxConfig", config);
    }

    // ── Simples Nacional ──────────────────────────────────────────────────

    @Test
    void calculate_simplesNacional_creditCard_correctBreakdown() {
        TaxBreakdown b = service.calculate(new BigDecimal("149.90"), "CREDIT_CARD");

        assertThat(b.taxRegime()).isEqualTo("SIMPLES_NACIONAL");
        assertThat(b.taxRateApplied()).isEqualByComparingTo("6.0");
        // processing fee = 149.90 * 4.98% = 7.47 (rounded)
        assertThat(b.processingFee()).isEqualByComparingTo("7.47");
        // tax = 149.90 * 6% = 8.99
        assertThat(b.taxAmount()).isEqualByComparingTo("8.99");
        // ISS included in simples → 0
        assertThat(b.issAmount()).isEqualByComparingTo("0.00");
        // net = 149.90 - 7.47 - 8.99 = 133.44
        assertThat(b.netAmount()).isEqualByComparingTo("133.44");
    }

    @Test
    void calculate_simplesNacional_pix_lowerFee() {
        TaxBreakdown b = service.calculate(new BigDecimal("149.90"), "PIX");

        // processing fee = 149.90 * 0.99% = 1.48
        assertThat(b.processingFee()).isEqualByComparingTo("1.48");
        assertThat(b.taxAmount()).isEqualByComparingTo("8.99");
        assertThat(b.issAmount()).isEqualByComparingTo("0.00");
        // net = 149.90 - 1.48 - 8.99 = 139.43
        assertThat(b.netAmount()).isEqualByComparingTo("139.43");
    }

    @Test
    void calculate_simplesNacional_debitCard_correctFee() {
        TaxBreakdown b = service.calculate(new BigDecimal("100.00"), "DEBIT_CARD");

        // processing fee = 100 * 1.99% = 1.99
        assertThat(b.processingFee()).isEqualByComparingTo("1.99");
        // tax = 100 * 6% = 6.00
        assertThat(b.taxAmount()).isEqualByComparingTo("6.00");
        // net = 100 - 1.99 - 6.00 = 92.01
        assertThat(b.netAmount()).isEqualByComparingTo("92.01");
    }

    @Test
    void calculate_nullPaymentMethod_defaultsToCreditCard() {
        TaxBreakdown b = service.calculate(new BigDecimal("100.00"), null);
        // default = credit card fee 4.98%
        assertThat(b.processingFee()).isEqualByComparingTo("4.98");
    }

    // ── Lucro Presumido ───────────────────────────────────────────────────

    @Test
    void calculate_lucroPresumido_includesIss() {
        config.setRegime("LUCRO_PRESUMIDO");

        TaxBreakdown b = service.calculate(new BigDecimal("149.90"), "CREDIT_CARD");

        assertThat(b.taxRegime()).isEqualTo("LUCRO_PRESUMIDO");
        // base rate = 4.80 + 2.88 + 0.65 + 3.00 = 11.33%
        assertThat(b.taxRateApplied()).isEqualByComparingTo("11.33");
        // tax = 149.90 * 11.33% = 16.98
        assertThat(b.taxAmount()).isEqualByComparingTo("16.98");
        // ISS = 149.90 * 2.9% = 4.35
        assertThat(b.issAmount()).isEqualByComparingTo("4.35");
        // fee = 149.90 * 4.98% = 7.47
        assertThat(b.processingFee()).isEqualByComparingTo("7.47");
        // net = 149.90 - 7.47 - 16.98 - 4.35 = 121.10
        assertThat(b.netAmount()).isEqualByComparingTo("121.10");
    }

    @Test
    void calculate_lucroPresumido_pix_noIssDoubleCount() {
        config.setRegime("LUCRO_PRESUMIDO");

        TaxBreakdown b = service.calculate(new BigDecimal("100.00"), "PIX");

        // PIX fee = 0.99
        assertThat(b.processingFee()).isEqualByComparingTo("0.99");
        // ISS separate
        assertThat(b.issAmount()).isEqualByComparingTo("2.90");
        assertThat(b.taxRegime()).isEqualTo("LUCRO_PRESUMIDO");
    }

    // ── net never negative ────────────────────────────────────────────────

    @Test
    void calculate_netAmountNeverNegative() {
        // pathological: tiny gross → net floors at 0
        TaxBreakdown b = service.calculate(new BigDecimal("0.01"), "CREDIT_CARD");
        assertThat(b.netAmount()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }
}
