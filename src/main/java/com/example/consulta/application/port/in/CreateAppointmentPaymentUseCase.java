package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.appointment.PaymentResponseDTO;

import java.math.BigDecimal;

public interface CreateAppointmentPaymentUseCase {

    PaymentResponseDTO execute(String appointmentId, String patientUserId, BigDecimal amount);
}
