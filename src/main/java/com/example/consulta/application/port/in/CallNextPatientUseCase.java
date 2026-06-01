package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;

public interface CallNextPatientUseCase {

    AppointmentResponseDTO execute(String appointmentId, String professionalUserId);
}
