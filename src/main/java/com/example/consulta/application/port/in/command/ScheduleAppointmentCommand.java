package com.example.consulta.application.port.in.command;

import com.example.consulta.domain.enums.AppointmentModality;
import com.example.consulta.domain.enums.PaymentMethod;

import java.time.LocalDateTime;

public record ScheduleAppointmentCommand(
        String userId,
        String professionalId,
        LocalDateTime scheduledAt,
        String reason,
        String notes,
        AppointmentModality modality,
        String serviceId,
        PaymentMethod chosenPaymentMethod
) {}
