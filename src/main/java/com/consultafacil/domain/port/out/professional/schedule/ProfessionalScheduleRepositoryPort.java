package com.consultafacil.domain.port.out.professional.schedule;

import com.consultafacil.domain.entity.ProfessionalSchedule;

import java.util.List;
import java.util.Optional;

public interface ProfessionalScheduleRepositoryPort {

    ProfessionalSchedule save(ProfessionalSchedule schedule);

    List<ProfessionalSchedule> findByProfessionalId(String professionalId);

    Optional<ProfessionalSchedule> findByProfessionalIdAndDayOfWeek(String professionalId, String dayOfWeek);
}
