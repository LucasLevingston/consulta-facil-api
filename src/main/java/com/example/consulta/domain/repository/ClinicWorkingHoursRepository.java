package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.ClinicWorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicWorkingHoursRepository extends JpaRepository<ClinicWorkingHours, String> {
    List<ClinicWorkingHours> findByClinicId(String clinicId);
    Optional<ClinicWorkingHours> findByClinicIdAndDayOfWeek(String clinicId, String dayOfWeek);
}
