package com.consultafacil.application.service.analytics;

import com.consultafacil.api.dto.analytics.BreakdownDTO;
import com.consultafacil.api.dto.analytics.FinancialAnalyticsDTO;
import com.consultafacil.api.dto.analytics.KpiDTO;
import com.consultafacil.api.dto.analytics.TimeSeriesDTO;
import com.consultafacil.application.port.in.analytics.FinancialAnalyticsUseCase;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.repository.analytics.FinancialAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialAnalyticsService implements FinancialAnalyticsUseCase {

    private final FinancialAnalyticsRepository repo;

    @Override
    @Transactional(readOnly = true)
    public FinancialAnalyticsDTO getFinancialAnalytics() {
        LocalDateTime since = LocalDateTime.now().minusMonths(12);

        BigDecimal totalRevenue = coalesce(repo.sumAmountByStatus(BillingPaymentStatus.PAID));
        BigDecimal netRevenue = coalesce(repo.sumNetAmountByStatus(BillingPaymentStatus.PAID));
        BigDecimal systemFees = coalesce(repo.sumSystemFeeByStatus(BillingPaymentStatus.PAID));
        BigDecimal refunded = coalesce(repo.sumAmountByStatus(BillingPaymentStatus.REFUNDED));
        long paidCount = repo.countByStatusParam(BillingPaymentStatus.PAID);

        List<KpiDTO> kpis = List.of(
                KpiDTO.currency("Receita Total", totalRevenue),
                KpiDTO.currency("Receita Liquida", netRevenue),
                KpiDTO.currency("Taxas do Sistema", systemFees),
                KpiDTO.currency("Total Reembolsado", refunded),
                KpiDTO.count("Pagamentos Realizados", paidCount)
        );

        List<TimeSeriesDTO> revenueSeries = repo.revenueByMonth(BillingPaymentStatus.PAID, since).stream()
                .map(r -> new TimeSeriesDTO(
                        formatMonthLabel((Number) r[0], (Number) r[1]),
                        r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO
                )).toList();

        List<BreakdownDTO> statusBreakdown = BreakdownDTO.from(repo.groupByStatus());
        List<BreakdownDTO> paymentTypeBreakdown = BreakdownDTO.from(repo.groupByPaymentType(BillingPaymentStatus.PAID));

        return new FinancialAnalyticsDTO(kpis, revenueSeries, statusBreakdown, paymentTypeBreakdown);
    }

    private BigDecimal coalesce(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String formatMonthLabel(Number year, Number month) {
        return String.format("%02d/%d", month.intValue(), year.intValue());
    }
}
