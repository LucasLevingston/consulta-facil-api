package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.examlab.CreateExamLabDTO;
import com.consultafacil.api.dto.examlab.ExamLabResponseDTO;

public interface CreateExamLabUseCase {
    ExamLabResponseDTO execute(CreateExamLabDTO dto);
}
