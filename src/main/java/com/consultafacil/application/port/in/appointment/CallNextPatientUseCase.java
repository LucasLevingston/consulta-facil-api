package com.consultafacil.application.port.in.appointment;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;

public interface CallNextPatientUseCase {

    AppointmentResponseDTO execute(String appointmentId, String professionalUserId);
}
