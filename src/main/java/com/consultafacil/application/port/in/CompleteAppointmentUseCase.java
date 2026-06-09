package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;

public interface CompleteAppointmentUseCase {

    AppointmentResponseDTO complete(String appointmentId, String professionalUserId);
}
