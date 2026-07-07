package com.consultafacil.application.port.in.appointment;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;

import java.util.List;

public interface GetClinicQueueUseCase {

    List<AppointmentResponseDTO> execute(String clinicId);
}
