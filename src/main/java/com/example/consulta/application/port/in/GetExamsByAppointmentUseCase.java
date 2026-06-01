package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.exam.ExamRequestResponseDTO;

import java.util.List;

public interface GetExamsByAppointmentUseCase {

    List<ExamRequestResponseDTO> execute(String appointmentId);
}
