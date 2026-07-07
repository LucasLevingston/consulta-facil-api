package com.consultafacil.application.port.in.patient;

import com.consultafacil.api.dto.patient.EmergencyContactDTO;

import java.util.List;

public interface ListEmergencyContactsUseCase {

    List<EmergencyContactDTO> execute(String userId);
}
