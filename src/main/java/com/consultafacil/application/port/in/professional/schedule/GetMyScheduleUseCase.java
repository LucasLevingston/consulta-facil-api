package com.consultafacil.application.port.in.professional.schedule;

import com.consultafacil.api.dto.schedule.ProfessionalScheduleResponseDTO;

import java.util.List;

public interface GetMyScheduleUseCase {
    List<ProfessionalScheduleResponseDTO> execute(String userId);
}
