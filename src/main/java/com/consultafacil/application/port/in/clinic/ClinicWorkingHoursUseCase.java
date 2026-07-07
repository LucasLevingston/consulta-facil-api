package com.consultafacil.application.port.in.clinic;

import com.consultafacil.api.dto.schedule.ClinicWorkingHoursResponseDTO;
import com.consultafacil.api.dto.schedule.CreateClinicWorkingHoursDTO;

import java.util.List;

public interface ClinicWorkingHoursUseCase {

    List<ClinicWorkingHoursResponseDTO> getWorkingHours(String clinicId);

    List<ClinicWorkingHoursResponseDTO> saveWorkingHours(String clinicId, String userId,
                                                          List<CreateClinicWorkingHoursDTO> dtos);
}
