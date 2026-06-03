package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;

public interface GenerateMeetLinkUseCase {

    AppointmentResponseDTO execute(String appointmentId, String professionalUserId);
}
