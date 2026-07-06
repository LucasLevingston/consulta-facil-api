package com.consultafacil.application.service;

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
class TaxMonthlyReportServiceTest {

    @Mock SubscriptionPaymentRepositoryPort paymentRepository;

    @InjectMocks TaxMonthlyReportService service;

    TaxConfig config;

    @BeforeEach
    void setUp() {
        config = new TaxConfig();
        org.springframework.test.util.ReflectionTestUtils.setField(service, "taxConfig", config);
    }

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
