package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.schedule.ProfessionalScheduleResponseDTO;

import java.util.List;

public interface GetProfessionalScheduleUseCase {
    List<ProfessionalScheduleResponseDTO> execute(String professionalId);
}
