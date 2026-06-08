package com.consultafacil.domain.event;

import java.math.BigDecimal;

public record PaymentFailedEvent(
        String eventId,
        String appointmentId,
        String patientId,
        String patientName,
        String patientEmail,
        BigDecimal amount,
        String currency,
        String occurredAt
) {}
