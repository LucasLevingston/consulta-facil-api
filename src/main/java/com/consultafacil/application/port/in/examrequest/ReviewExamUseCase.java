package com.consultafacil.application.port.in.examrequest;

import com.consultafacil.api.dto.exam.ExamRequestResponseDTO;
import com.consultafacil.api.dto.exam.ReviewExamRequestDTO;

public interface ReviewExamUseCase {

    ExamRequestResponseDTO execute(String examId, String professionalUserId, ReviewExamRequestDTO dto);
}
