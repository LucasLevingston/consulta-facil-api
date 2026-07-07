package com.consultafacil.application.port.in.examlab;

import com.consultafacil.api.dto.examlab.ExamLabResponseDTO;

public interface GetExamLabByIdUseCase {
    ExamLabResponseDTO execute(String id);
}
