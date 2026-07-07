package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.appointment.ClinicalNoteResponseDTO;
import com.consultafacil.api.dto.appointment.SaveClinicalNoteDTO;

public interface SaveClinicalNoteUseCase {

    ClinicalNoteResponseDTO execute(String appointmentId, String userId, SaveClinicalNoteDTO dto);
}
