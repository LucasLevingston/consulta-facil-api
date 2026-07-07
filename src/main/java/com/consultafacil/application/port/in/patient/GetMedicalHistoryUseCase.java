package com.consultafacil.application.port.in.patient;

import com.consultafacil.api.dto.appointment.MedicalHistoryResponseDTO;

import java.util.Optional;

public interface GetMedicalHistoryUseCase {

    Optional<MedicalHistoryResponseDTO> execute(String appointmentId, String authenticatedUserId);
}
