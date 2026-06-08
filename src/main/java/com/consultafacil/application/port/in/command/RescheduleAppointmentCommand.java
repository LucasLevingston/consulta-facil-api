package com.consultafacil.application.port.in.command;

import java.time.LocalDateTime;

public record RescheduleAppointmentCommand(
        String appointmentId,
        String authenticatedUserId,
        LocalDateTime newScheduledAt,
        String newReason
) {}
