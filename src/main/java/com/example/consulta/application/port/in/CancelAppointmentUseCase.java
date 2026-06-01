package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;
import com.example.consulta.application.port.in.command.CancelAppointmentCommand;

public interface CancelAppointmentUseCase {

    AppointmentResponseDTO execute(CancelAppointmentCommand command);
}
