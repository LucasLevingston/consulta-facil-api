package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.schedule.CreateProfessionalScheduleDTO;
import com.consultafacil.api.dto.schedule.ProfessionalScheduleResponseDTO;

import java.util.List;

public interface SaveMyScheduleUseCase {
    List<ProfessionalScheduleResponseDTO> execute(String userId, List<CreateProfessionalScheduleDTO> dtos);
}
