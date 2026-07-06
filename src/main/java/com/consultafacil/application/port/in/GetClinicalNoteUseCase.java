package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.appointment.ClinicalNoteResponseDTO;

import java.util.Optional;

public interface GetClinicalNoteUseCase {

    Optional<ClinicalNoteResponseDTO> execute(String appointmentId, String authenticatedUserId);
}
