package com.example.consulta.application.port.in.command;

public record RateAppointmentCommand(
        String appointmentId,
        String userId,
        int stars,
        String comment
) {}
