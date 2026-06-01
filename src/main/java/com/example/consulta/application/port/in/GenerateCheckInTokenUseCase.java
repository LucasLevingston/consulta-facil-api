package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.appointment.QrCheckInTokenDTO;

public interface GenerateCheckInTokenUseCase {

    QrCheckInTokenDTO execute(String appointmentId, String patientUserId);
}
