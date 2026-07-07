package com.consultafacil.application.port.in.examrequest;

import com.consultafacil.api.dto.examscheduling.ExamSchedulingResponseDTO;
import com.consultafacil.api.dto.examscheduling.ScheduleExamDTO;

public interface ScheduleExamUseCase {
    ExamSchedulingResponseDTO execute(String patientUserId, ScheduleExamDTO dto);
}
