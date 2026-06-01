package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;
import com.example.consulta.api.dto.appointment.SetModalityDTO;

public interface SetAppointmentModalityUseCase {

    AppointmentResponseDTO execute(String appointmentId, String professionalUserId, SetModalityDTO dto);
}
