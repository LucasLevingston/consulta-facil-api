package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;

public interface CompleteAppointmentUseCase {

    AppointmentResponseDTO complete(String appointmentId, String professionalUserId);
}
