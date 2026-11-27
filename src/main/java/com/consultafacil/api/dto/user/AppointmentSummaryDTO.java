package com.consultafacil.api.dto.user;

import java.time.LocalDateTime;

public record AppointmentSummaryDTO(
        String id,
        String status,
        LocalDateTime scheduledAt,
        String professionalName
) {}
