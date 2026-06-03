package com.example.consulta.domain.port.out;

import com.example.consulta.domain.entity.ProfessionalSchedule;

import java.util.List;
import java.util.Optional;

public interface ProfessionalScheduleRepositoryPort {

    ProfessionalSchedule save(ProfessionalSchedule schedule);

    List<ProfessionalSchedule> findByProfessionalId(String professionalId);

    Optional<ProfessionalSchedule> findByProfessionalIdAndDayOfWeek(String professionalId, String dayOfWeek);
}
