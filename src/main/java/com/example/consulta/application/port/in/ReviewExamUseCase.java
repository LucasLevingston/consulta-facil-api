package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.exam.ExamRequestResponseDTO;
import com.example.consulta.api.dto.exam.ReviewExamRequestDTO;

public interface ReviewExamUseCase {

    ExamRequestResponseDTO execute(String examId, String professionalUserId, ReviewExamRequestDTO dto);
}
