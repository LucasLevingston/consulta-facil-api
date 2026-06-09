package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.schedule.CreateProfessionalScheduleDTO;
import com.consultafacil.api.dto.schedule.ProfessionalScheduleResponseDTO;

import java.util.List;

public interface ProfessionalScheduleUseCase {

    List<ProfessionalScheduleResponseDTO> getByProfessionalId(String professionalId);

    List<ProfessionalScheduleResponseDTO> getMySchedule(String userId);

    List<ProfessionalScheduleResponseDTO> saveMySchedule(String userId,
                                                          List<CreateProfessionalScheduleDTO> dtos);
}
