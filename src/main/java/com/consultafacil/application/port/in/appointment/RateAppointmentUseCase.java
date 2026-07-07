package com.consultafacil.application.port.in.appointment;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.application.port.in.command.RateAppointmentCommand;

public interface RateAppointmentUseCase {

    AppointmentResponseDTO execute(RateAppointmentCommand command);
}
