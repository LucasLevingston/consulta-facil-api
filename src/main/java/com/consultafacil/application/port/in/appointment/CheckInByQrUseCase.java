package com.consultafacil.application.port.in.appointment;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;

public interface CheckInByQrUseCase {

    AppointmentResponseDTO execute(String token);
}
