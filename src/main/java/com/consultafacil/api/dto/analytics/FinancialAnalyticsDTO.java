package com.consultafacil.api.dto.analytics;

import java.util.List;

public record FinancialAnalyticsDTO(
        List<KpiDTO> kpis,
        List<TimeSeriesDTO> revenueSeries,
        List<BreakdownDTO> statusBreakdown,
        List<BreakdownDTO> paymentTypeBreakdown
) {}
