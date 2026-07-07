package com.consultafacil.api.dto.analytics;

import java.util.List;

public record SubscriptionAnalyticsDTO(
        List<KpiDTO> kpis,
        List<BreakdownDTO> statusBreakdown,
        List<BreakdownDTO> planBreakdown
) {}
