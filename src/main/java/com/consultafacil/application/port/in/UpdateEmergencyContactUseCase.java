package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.patient.EmergencyContactDTO;

public interface UpdateEmergencyContactUseCase {

    EmergencyContactDTO execute(String userId, String contactId, EmergencyContactDTO dto);
}
