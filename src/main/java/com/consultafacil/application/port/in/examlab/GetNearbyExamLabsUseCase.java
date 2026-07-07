package com.consultafacil.application.port.in.examlab;

import com.consultafacil.api.dto.examlab.ExamLabResponseDTO;

import java.util.List;

public interface GetNearbyExamLabsUseCase {
    List<ExamLabResponseDTO> execute(double lat, double lng, double radiusKm);
}
