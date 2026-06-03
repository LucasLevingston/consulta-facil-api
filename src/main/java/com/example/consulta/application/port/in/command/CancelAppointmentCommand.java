package com.example.consulta.application.port.in.command;

public record CancelAppointmentCommand(
        String appointmentId,
        String authenticatedUserId,
        String cancellationReason
) {}
