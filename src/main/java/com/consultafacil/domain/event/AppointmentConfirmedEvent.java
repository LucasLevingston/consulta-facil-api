package com.consultafacil.domain.event;

import com.consultafacil.domain.enums.AppointmentModality;

import java.time.LocalDateTime;

public record AppointmentConfirmedEvent(
        String eventId,
        String appointmentId,
        String patientId,
        String patientName,
        String patientEmail,
        String patientPhone,
        String professionalId,
        String professionalName,
        LocalDateTime scheduledAt,
        AppointmentModality modality,
        String occurredAt
) {}
