package com.consultafacil.application.port.in.examrequest;

import com.consultafacil.api.dto.exam.ExamRequestResponseDTO;

import java.util.List;

public interface GetExamsByAppointmentUseCase {

    List<ExamRequestResponseDTO> execute(String appointmentId);
}
