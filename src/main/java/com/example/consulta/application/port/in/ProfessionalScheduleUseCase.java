package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.schedule.CreateProfessionalScheduleDTO;
import com.example.consulta.api.dto.schedule.ProfessionalScheduleResponseDTO;

import java.util.List;

public interface ProfessionalScheduleUseCase {

    List<ProfessionalScheduleResponseDTO> getByProfessionalId(String professionalId);

    List<ProfessionalScheduleResponseDTO> getMySchedule(String userId);

    List<ProfessionalScheduleResponseDTO> saveMySchedule(String userId,
                                                          List<CreateProfessionalScheduleDTO> dtos);
}
