package com.consultafacil.application.port.in.appointment;

import com.consultafacil.api.dto.appointment.QrCheckInTokenDTO;

public interface GenerateCheckInTokenUseCase {

    QrCheckInTokenDTO execute(String appointmentId, String patientUserId);
}
