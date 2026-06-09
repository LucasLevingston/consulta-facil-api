package com.consultafacil.application.port.in.command;

import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.enums.PaymentMethod;

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
