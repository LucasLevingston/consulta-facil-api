package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.schedule.ClinicWorkingHoursResponseDTO;
import com.example.consulta.api.dto.schedule.CreateClinicWorkingHoursDTO;

import java.util.List;

public interface ClinicWorkingHoursUseCase {

    List<ClinicWorkingHoursResponseDTO> getWorkingHours(String clinicId);

    List<ClinicWorkingHoursResponseDTO> saveWorkingHours(String clinicId, String userId,
                                                          List<CreateClinicWorkingHoursDTO> dtos);
}
