package com.consultafacil.api.dto.analytics;

import java.util.List;

public record ReferralAnalyticsDTO(
        List<KpiDTO> kpis,
        List<TimeSeriesDTO> referralSeries,
        List<BreakdownDTO> statusBreakdown
) {}
