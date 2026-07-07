package com.consultafacil.domain.port.out.clinic;

import com.consultafacil.domain.entity.ClinicWorkingHours;

import java.util.List;
import java.util.Optional;

public interface ClinicWorkingHoursRepositoryPort {

    ClinicWorkingHours save(ClinicWorkingHours hours);

    List<ClinicWorkingHours> findByClinicId(String clinicId);

    Optional<ClinicWorkingHours> findByClinicIdAndDayOfWeek(String clinicId, String dayOfWeek);
}
