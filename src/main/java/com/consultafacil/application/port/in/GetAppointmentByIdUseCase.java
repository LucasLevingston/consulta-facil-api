package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;

public interface GetAppointmentByIdUseCase {

    AppointmentResponseDTO execute(String appointmentId, String authenticatedUserId);
}
