package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.appointment.MedicalHistoryResponseDTO;
import com.example.consulta.api.dto.appointment.SaveMedicalHistoryDTO;

import java.util.Optional;

public interface MedicalHistoryUseCase {

    Optional<MedicalHistoryResponseDTO> getByAppointmentId(String appointmentId, String authenticatedUserId);

    MedicalHistoryResponseDTO save(String appointmentId, String userId, SaveMedicalHistoryDTO dto);
}
