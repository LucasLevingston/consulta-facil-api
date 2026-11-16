package com.consultafacil.application.service.analytics;

import com.consultafacil.api.dto.analytics.FinancialAnalyticsDTO;
import com.consultafacil.api.dto.analytics.KpiDTO;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.port.out.analytics.FinancialAnalyticsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialAnalyticsServiceTest {

    @Mock FinancialAnalyticsPort repo;

    FinancialAnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new FinancialAnalyticsService(repo);
        when(repo.groupByStatus()).thenReturn(List.of());
        when(repo.groupByPaymentType(BillingPaymentStatus.PAID)).thenReturn(List.of());
    }

    @Test
    void execute_buildsCurrencyKpisFromPaidAndRefundedTotals() {
        when(repo.sumAmountByStatus(BillingPaymentStatus.PAID)).thenReturn(new BigDecimal("1500.00"));
        when(repo.sumNetAmountByStatus(BillingPaymentStatus.PAID)).thenReturn(new BigDecimal("1350.00"));
        when(repo.sumSystemFeeByStatus(BillingPaymentStatus.PAID)).thenReturn(new BigDecimal("150.00"));
        when(repo.sumAmountByStatus(BillingPaymentStatus.REFUNDED)).thenReturn(new BigDecimal("80.00"));
        when(repo.countByStatusParam(BillingPaymentStatus.PAID)).thenReturn(12L);
        when(repo.revenueByMonth(eq(BillingPaymentStatus.PAID), any())).thenReturn(List.of());

        FinancialAnalyticsDTO result = service.getFinancialAnalytics();

        assertThat(result.kpis()).extracting(KpiDTO::label).containsExactly(
                "Receita Total", "Receita Liquida", "Taxas do Sistema", "Total Reembolsado", "Pagamentos Realizados");
        assertThat(result.kpis().get(0).value()).isEqualByComparingTo("1500.00");
        assertThat(result.kpis().get(0).unit()).isEqualTo("BRL");
        assertThat(result.kpis().get(1).value()).isEqualByComparingTo("1350.00");
        assertThat(result.kpis().get(2).value()).isEqualByComparingTo("150.00");
        assertThat(result.kpis().get(3).value()).isEqualByComparingTo("80.00");
        assertThat(result.kpis().get(4).value()).isEqualByComparingTo("12");
        assertThat(result.kpis().get(4).unit()).isEqualTo("count");
    }

    @Test
    void execute_treatsNullSumsAsZero() {
        when(repo.sumAmountByStatus(any())).thenReturn(null);
        when(repo.sumNetAmountByStatus(BillingPaymentStatus.PAID)).thenReturn(null);
        when(repo.sumSystemFeeByStatus(BillingPaymentStatus.PAID)).thenReturn(null);
        when(repo.countByStatusParam(BillingPaymentStatus.PAID)).thenReturn(0L);
        when(repo.revenueByMonth(eq(BillingPaymentStatus.PAID), any())).thenReturn(List.of());

        FinancialAnalyticsDTO result = service.getFinancialAnalytics();

        assertThat(result.kpis().subList(0, 4))
                .allSatisfy(kpi -> assertThat(kpi.value()).isEqualByComparingTo(BigDecimal.ZERO));
    }

    @Test
    void execute_mapsRevenueSeriesAndDefaultsNullMonthTotalsToZero() {
        when(repo.sumAmountByStatus(any())).thenReturn(BigDecimal.ZERO);
        when(repo.sumNetAmountByStatus(BillingPaymentStatus.PAID)).thenReturn(BigDecimal.ZERO);
        when(repo.sumSystemFeeByStatus(BillingPaymentStatus.PAID)).thenReturn(BigDecimal.ZERO);
        when(repo.countByStatusParam(BillingPaymentStatus.PAID)).thenReturn(0L);
        when(repo.revenueByMonth(eq(BillingPaymentStatus.PAID), any())).thenReturn(List.<Object[]>of(
                new Object[]{2026, 1, new BigDecimal("300.50")},
                new Object[]{2026, 2, null}));

        FinancialAnalyticsDTO result = service.getFinancialAnalytics();

        assertThat(result.revenueSeries()).hasSize(2);
        assertThat(result.revenueSeries().get(0).label()).isEqualTo("01/2026");
        assertThat(result.revenueSeries().get(0).value()).isEqualByComparingTo("300.50");
        assertThat(result.revenueSeries().get(1).label()).isEqualTo("02/2026");
        assertThat(result.revenueSeries().get(1).value()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void execute_buildsStatusAndPaymentTypeBreakdowns() {
        when(repo.sumAmountByStatus(any())).thenReturn(BigDecimal.ZERO);
        when(repo.sumNetAmountByStatus(BillingPaymentStatus.PAID)).thenReturn(BigDecimal.ZERO);
        when(repo.sumSystemFeeByStatus(BillingPaymentStatus.PAID)).thenReturn(BigDecimal.ZERO);
        when(repo.countByStatusParam(BillingPaymentStatus.PAID)).thenReturn(0L);
        when(repo.revenueByMonth(eq(BillingPaymentStatus.PAID), any())).thenReturn(List.of());
        when(repo.groupByStatus()).thenReturn(List.<Object[]>of(new Object[]{"PAID", 3L}, new Object[]{"PENDING", 1L}));
        when(repo.groupByPaymentType(BillingPaymentStatus.PAID)).thenReturn(List.<Object[]>of(new Object[]{"CONSULTATION", 3L}));

        FinancialAnalyticsDTO result = service.getFinancialAnalytics();

        assertThat(result.statusBreakdown()).hasSize(2);
        assertThat(result.statusBreakdown().get(0).percentage()).isEqualByComparingTo("75.0");
        assertThat(result.paymentTypeBreakdown()).singleElement()
                .satisfies(b -> {
                    assertThat(b.label()).isEqualTo("CONSULTATION");
                    assertThat(b.percentage()).isEqualByComparingTo("100.0");
                });
    }
}
