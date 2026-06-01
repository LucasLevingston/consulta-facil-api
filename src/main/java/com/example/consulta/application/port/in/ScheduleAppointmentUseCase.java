package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;
import com.example.consulta.application.port.in.command.ScheduleAppointmentCommand;

public interface ScheduleAppointmentUseCase {

    AppointmentResponseDTO execute(ScheduleAppointmentCommand command);
}
