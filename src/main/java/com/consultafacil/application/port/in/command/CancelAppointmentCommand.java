package com.consultafacil.application.port.in.command;

public record CancelAppointmentCommand(
        String appointmentId,
        String authenticatedUserId,
        String cancellationReason
) {}
