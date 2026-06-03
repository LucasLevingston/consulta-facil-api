package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.appointment.ClinicalNoteResponseDTO;
import com.example.consulta.api.dto.appointment.SaveClinicalNoteDTO;

import java.util.Optional;

public interface ClinicalNoteUseCase {

    Optional<ClinicalNoteResponseDTO> getByAppointmentId(String appointmentId, String authenticatedUserId);

    ClinicalNoteResponseDTO save(String appointmentId, String userId, SaveClinicalNoteDTO dto);
}
