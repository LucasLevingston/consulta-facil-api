package com.consultafacil.application.port.in.patient;

import com.consultafacil.api.dto.patient.EmergencyContactDTO;

public interface AddEmergencyContactUseCase {

    EmergencyContactDTO execute(String userId, EmergencyContactDTO dto);
}
