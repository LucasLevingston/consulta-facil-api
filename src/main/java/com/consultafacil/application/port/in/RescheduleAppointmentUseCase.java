package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.application.port.in.command.RescheduleAppointmentCommand;

public interface RescheduleAppointmentUseCase {

    AppointmentResponseDTO execute(RescheduleAppointmentCommand command);
}
