package com.example.consulta.core.messaging.event;

import com.example.consulta.domain.enums.AppointmentModality;

import java.time.LocalDateTime;

public record AppointmentCreatedEvent(
        String eventId,
        String appointmentId,
        String patientId,
        String patientName,
        String patientEmail,
        String patientPhone,
        String professionalId,
        String professionalName,
        String professionalEmail,
        String professionalPhone,
        LocalDateTime scheduledAt,
        AppointmentModality modality,
        String occurredAt
) {}
