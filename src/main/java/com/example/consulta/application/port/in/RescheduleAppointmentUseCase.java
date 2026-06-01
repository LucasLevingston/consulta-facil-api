package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;
import com.example.consulta.application.port.in.command.RescheduleAppointmentCommand;

public interface RescheduleAppointmentUseCase {

    AppointmentResponseDTO execute(RescheduleAppointmentCommand command);
}
