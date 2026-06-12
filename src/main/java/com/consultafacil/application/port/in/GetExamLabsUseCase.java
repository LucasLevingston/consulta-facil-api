package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.examlab.ExamLabResponseDTO;

import java.util.List;

public interface GetExamLabsUseCase {
    List<ExamLabResponseDTO> execute();

    ExamLabResponseDTO executeById(String id);
}
