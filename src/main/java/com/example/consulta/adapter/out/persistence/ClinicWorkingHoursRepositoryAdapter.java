package com.example.consulta.adapter.out.persistence;

import com.example.consulta.domain.entity.ClinicWorkingHours;
import com.example.consulta.domain.port.out.ClinicWorkingHoursRepositoryPort;
import com.example.consulta.domain.repository.ClinicWorkingHoursRepository;
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
