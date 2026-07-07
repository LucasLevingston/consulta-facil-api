package com.consultafacil.application.port.in.examrequest;

import com.consultafacil.api.dto.exam.CreateExamRequestDTO;
import com.consultafacil.api.dto.exam.ExamRequestResponseDTO;

public interface RequestExamUseCase {

    ExamRequestResponseDTO execute(String appointmentId, String professionalUserId, CreateExamRequestDTO dto);
}
