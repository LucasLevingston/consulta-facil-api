package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.appointment.ClinicalNoteResponseDTO;
import com.consultafacil.api.dto.appointment.SaveClinicalNoteDTO;

import java.util.Optional;

public interface ClinicalNoteUseCase {

    Optional<ClinicalNoteResponseDTO> getByAppointmentId(String appointmentId, String authenticatedUserId);

    ClinicalNoteResponseDTO save(String appointmentId, String userId, SaveClinicalNoteDTO dto);
}
