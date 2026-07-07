package com.consultafacil.application.port.in.examlab;

import com.consultafacil.api.dto.examlab.ExamLabHoursDTO;
import com.consultafacil.api.dto.examlab.ExamLabResponseDTO;

import java.util.List;

public interface SetExamLabHoursUseCase {
    ExamLabResponseDTO execute(String examLabId, List<ExamLabHoursDTO> hours);
}
