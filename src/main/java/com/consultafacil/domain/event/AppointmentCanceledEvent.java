package com.consultafacil.domain.event;

import com.consultafacil.domain.enums.AppointmentModality;

import java.time.LocalDateTime;

public record AppointmentCanceledEvent(
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
        String cancellationReason,
        String occurredAt
) {}
