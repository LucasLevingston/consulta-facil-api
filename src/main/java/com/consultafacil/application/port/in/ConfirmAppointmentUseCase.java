package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;

public interface ConfirmAppointmentUseCase {

    AppointmentResponseDTO confirm(String appointmentId, String professionalUserId);
}
