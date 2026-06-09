package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.ClinicWorkingHours;
import com.consultafacil.domain.port.out.ClinicWorkingHoursRepositoryPort;
import com.consultafacil.domain.repository.ClinicWorkingHoursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClinicWorkingHoursRepositoryAdapter implements ClinicWorkingHoursRepositoryPort {

    private final ClinicWorkingHoursRepository clinicWorkingHoursRepository;

    @Override
    public ClinicWorkingHours save(ClinicWorkingHours hours) {
        return clinicWorkingHoursRepository.save(hours);
    }

    @Override
    public List<ClinicWorkingHours> findByClinicId(String clinicId) {
        return clinicWorkingHoursRepository.findByClinicId(clinicId);
    }

    @Override
    public Optional<ClinicWorkingHours> findByClinicIdAndDayOfWeek(String clinicId, String dayOfWeek) {
        return clinicWorkingHoursRepository.findByClinicIdAndDayOfWeek(clinicId, dayOfWeek);
    }
}
