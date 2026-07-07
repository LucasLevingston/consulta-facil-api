package com.consultafacil.application.port.in.appointment;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.api.dto.appointment.SetModalityDTO;

public interface SetAppointmentModalityUseCase {

    AppointmentResponseDTO execute(String appointmentId, String professionalUserId, SetModalityDTO dto);
}
