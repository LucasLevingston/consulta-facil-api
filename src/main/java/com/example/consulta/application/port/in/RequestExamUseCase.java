package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.exam.CreateExamRequestDTO;
import com.example.consulta.api.dto.exam.ExamRequestResponseDTO;

public interface RequestExamUseCase {

    ExamRequestResponseDTO execute(String appointmentId, String professionalUserId, CreateExamRequestDTO dto);
}
