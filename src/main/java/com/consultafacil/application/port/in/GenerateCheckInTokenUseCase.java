package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.appointment.QrCheckInTokenDTO;

public interface GenerateCheckInTokenUseCase {

    QrCheckInTokenDTO execute(String appointmentId, String patientUserId);
}
