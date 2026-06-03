package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;

import java.util.List;

public interface GetClinicQueueUseCase {

    List<AppointmentResponseDTO> execute(String clinicId);
}
