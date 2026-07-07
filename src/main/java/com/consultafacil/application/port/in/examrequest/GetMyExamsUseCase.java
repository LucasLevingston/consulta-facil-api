package com.consultafacil.application.port.in.examrequest;

import com.consultafacil.api.dto.exam.ExamRequestResponseDTO;
import com.consultafacil.domain.enums.ExamRequestStatus;

import java.util.List;

public interface GetMyExamsUseCase {

    List<ExamRequestResponseDTO> execute(String userId, ExamRequestStatus status);
}
