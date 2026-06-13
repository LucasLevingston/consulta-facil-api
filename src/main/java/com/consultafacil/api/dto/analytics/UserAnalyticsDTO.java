package com.consultafacil.api.dto.analytics;

import java.util.List;

public record UserAnalyticsDTO(
        List<KpiDTO> kpis,
        List<TimeSeriesDTO> growthSeries,
        List<BreakdownDTO> roleBreakdown
) {}
