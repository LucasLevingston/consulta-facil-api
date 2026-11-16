package com.consultafacil.api.dto.analytics.professional;

import java.math.BigDecimal;
import java.util.List;

/** Revenue from PAID appointments in the period; byMonth covers every month of the window (zero-filled). */
public record RevenueStatsDTO(
        BigDecimal total,
        BigDecimal avgPerAppointment,
        long paidAppointments,
        List<MonthlyRevenueDTO> byMonth
) {}
