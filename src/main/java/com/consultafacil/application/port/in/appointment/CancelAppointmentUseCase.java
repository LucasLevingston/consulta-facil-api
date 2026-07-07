package com.consultafacil.application.port.in.appointment;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.application.port.in.command.CancelAppointmentCommand;

public interface CancelAppointmentUseCase {

    AppointmentResponseDTO execute(CancelAppointmentCommand command);
}
