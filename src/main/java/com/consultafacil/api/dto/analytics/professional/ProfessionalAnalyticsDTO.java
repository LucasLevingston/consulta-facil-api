package com.consultafacil.api.dto.analytics.professional;

import java.time.LocalDateTime;
import java.util.List;

public record ProfessionalAnalyticsDTO(
        String period,
        LocalDateTime from,
        LocalDateTime to,
        RevenueStatsDTO revenue,
        AppointmentStatsDTO appointments,
        RatingStatsDTO ratings,
        List<TopServiceDTO> topServices
) {}
