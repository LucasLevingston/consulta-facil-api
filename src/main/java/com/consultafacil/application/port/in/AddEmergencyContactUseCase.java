package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.patient.EmergencyContactDTO;

public interface AddEmergencyContactUseCase {

    EmergencyContactDTO execute(String userId, EmergencyContactDTO dto);
}
