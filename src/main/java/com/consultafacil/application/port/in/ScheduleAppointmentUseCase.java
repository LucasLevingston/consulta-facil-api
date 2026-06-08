package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.application.port.in.command.ScheduleAppointmentCommand;

public interface ScheduleAppointmentUseCase {

    AppointmentResponseDTO execute(ScheduleAppointmentCommand command);
}
