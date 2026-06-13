package com.consultafacil.api.dto.analytics;

import java.util.List;

public record AppointmentAnalyticsDTO(
        List<KpiDTO> kpis,
        List<TimeSeriesDTO> appointmentSeries,
        List<BreakdownDTO> statusBreakdown,
        List<BreakdownDTO> modalityBreakdown
) {}
