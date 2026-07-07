package com.consultafacil.application.port.in.examlab;

import com.consultafacil.api.dto.examlab.ExamLabResponseDTO;

import java.util.List;

public interface GetExamLabsUseCase {
    List<ExamLabResponseDTO> execute();
}
