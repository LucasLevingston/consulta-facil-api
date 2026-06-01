package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;

public interface ConfirmAppointmentUseCase {

    AppointmentResponseDTO confirm(String appointmentId, String professionalUserId);
}
