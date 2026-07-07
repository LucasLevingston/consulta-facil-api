package com.consultafacil.application.port.in.appointment;

import com.consultafacil.api.dto.appointment.PaymentResponseDTO;

import java.math.BigDecimal;

public interface CreateAppointmentPaymentUseCase {

    PaymentResponseDTO execute(String appointmentId, String patientUserId, BigDecimal amount);
}
