package com.example.consulta.domain.event;

import java.math.BigDecimal;

public record PaymentSucceededEvent(
        String eventId,
        String appointmentId,
        String patientId,
        String patientName,
        String patientEmail,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String mercadopagoPaymentId,
        String occurredAt
) {}
