package com.consultafacil.api.dto.analytics.professional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * confirmed = every appointment that is neither PENDING nor CANCELED (includes COMPLETED).
 * cancellationRate is a 0..1 ratio. byWeekday counts non-canceled appointments, MONDAY..SUNDAY.
 */
public record AppointmentStatsDTO(
        long total,
        long confirmed,
        long completed,
        long canceled,
        long pending,
        BigDecimal cancellationRate,
        Map<String, Long> byModality,
        List<WeekdayCountDTO> byWeekday
) {}
