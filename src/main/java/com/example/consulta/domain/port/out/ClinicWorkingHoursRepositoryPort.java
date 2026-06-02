package com.example.consulta.domain.port.out;

import com.example.consulta.domain.entity.ClinicWorkingHours;

import java.util.List;
import java.util.Optional;

public interface ClinicWorkingHoursRepositoryPort {

    ClinicWorkingHours save(ClinicWorkingHours hours);

    List<ClinicWorkingHours> findByClinicId(String clinicId);

    Optional<ClinicWorkingHours> findByClinicIdAndDayOfWeek(String clinicId, String dayOfWeek);
}
