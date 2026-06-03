package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;
import com.example.consulta.application.port.in.command.RateAppointmentCommand;

public interface RateAppointmentUseCase {

    AppointmentResponseDTO execute(RateAppointmentCommand command);
}
