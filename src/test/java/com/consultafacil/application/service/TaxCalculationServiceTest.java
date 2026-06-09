package com.consultafacil.application.service;

import com.consultafacil.api.dto.tax.TaxBreakdown;
import com.consultafacil.api.dto.tax.TaxReportDTO;
import com.consultafacil.core.config.TaxConfig;
import com.consultafacil.domain.entity.SubscriptionPayment;
import com.consultafacil.domain.port.out.SubscriptionPaymentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaxCalculationServiceTest {

    @Mock SubscriptionPaymentRepositoryPort paymentRepository;

    @InjectMocks TaxCalculationService service;

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

    // ── snapshot ──────────────────────────────────────────────────────────

    @Test
    void buildSnapshot_containsRegimeAndMethod() {
        TaxBreakdown b = service.calculate(new BigDecimal("149.90"), "PIX");
        String snapshot = service.buildSnapshot(b);
        assertThat(snapshot).contains("SIMPLES_NACIONAL");
        assertThat(snapshot).contains("PIX");
    }

    // ── monthly report ────────────────────────────────────────────────────

    @Test
    void monthlyReport_aggregatesCorrectly() {
        SubscriptionPayment p1 = SubscriptionPayment.builder()
                .subscriptionId("s1").grossAmount(new BigDecimal("149.90"))
                .processingFee(new BigDecimal("7.47")).taxAmount(new BigDecimal("8.99"))
                .issAmount(BigDecimal.ZERO).netAmount(new BigDecimal("133.44"))
                .paymentMethod("CREDIT_CARD").paidAt(LocalDateTime.now()).build();
        SubscriptionPayment p2 = SubscriptionPayment.builder()
                .subscriptionId("s2").grossAmount(new BigDecimal("149.90"))
                .processingFee(new BigDecimal("1.48")).taxAmount(new BigDecimal("8.99"))
                .issAmount(BigDecimal.ZERO).netAmount(new BigDecimal("139.43"))
                .paymentMethod("PIX").paidAt(LocalDateTime.now()).build();

        when(paymentRepository.findByPaidAtBetween(any(), any())).thenReturn(List.of(p1, p2));

        TaxReportDTO report = service.monthlyReport(2026, 6);

        assertThat(report.getTransactionCount()).isEqualTo(2);
        assertThat(report.getTotalGross()).isEqualByComparingTo("299.80");
        assertThat(report.getTotalProcessingFees()).isEqualByComparingTo("8.95");
        assertThat(report.getTotalTax()).isEqualByComparingTo("17.98");
        assertThat(report.getTotalNet()).isEqualByComparingTo("272.87");
        assertThat(report.getByPaymentMethod()).containsKey("CREDIT_CARD");
        assertThat(report.getByPaymentMethod()).containsKey("PIX");
    }

    @Test
    void monthlyReport_emptyMonth_returnsZeros() {
        when(paymentRepository.findByPaidAtBetween(any(), any())).thenReturn(List.of());

        TaxReportDTO report = service.monthlyReport(2026, 1);

        assertThat(report.getTransactionCount()).isEqualTo(0);
        assertThat(report.getTotalGross()).isEqualByComparingTo("0");
        assertThat(report.getTotalNet()).isEqualByComparingTo("0");
    }
}
