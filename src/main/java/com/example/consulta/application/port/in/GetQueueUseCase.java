package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;

import java.util.List;

public interface GetQueueUseCase {

    List<AppointmentResponseDTO> execute(String userId, String role);
}
