package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.appointment.MedicalHistoryResponseDTO;
import com.consultafacil.api.dto.appointment.SaveMedicalHistoryDTO;

public interface SaveMedicalHistoryUseCase {

    MedicalHistoryResponseDTO execute(String appointmentId, String userId, SaveMedicalHistoryDTO dto);
}
