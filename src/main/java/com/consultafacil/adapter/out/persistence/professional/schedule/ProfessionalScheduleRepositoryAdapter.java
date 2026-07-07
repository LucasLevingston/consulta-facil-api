package com.consultafacil.adapter.out.persistence.professional.schedule;

import com.consultafacil.domain.entity.ProfessionalSchedule;
import com.consultafacil.domain.port.out.professional.schedule.ProfessionalScheduleRepositoryPort;
import com.consultafacil.domain.repository.professional.schedule.ProfessionalScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProfessionalScheduleRepositoryAdapter implements ProfessionalScheduleRepositoryPort {

    private final ProfessionalScheduleRepository professionalScheduleRepository;

    @Override
    public ProfessionalSchedule save(ProfessionalSchedule schedule) {
        return professionalScheduleRepository.save(schedule);
    }

    @Override
    public List<ProfessionalSchedule> findByProfessionalId(String professionalId) {
        return professionalScheduleRepository.findByProfessionalId(professionalId);
    }

    @Override
    public Optional<ProfessionalSchedule> findByProfessionalIdAndDayOfWeek(
            String professionalId, String dayOfWeek) {
        return professionalScheduleRepository.findByProfessionalIdAndDayOfWeek(professionalId, dayOfWeek);
    }
}
