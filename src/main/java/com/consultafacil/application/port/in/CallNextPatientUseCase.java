package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;

public interface CallNextPatientUseCase {

    AppointmentResponseDTO execute(String appointmentId, String professionalUserId);
}
