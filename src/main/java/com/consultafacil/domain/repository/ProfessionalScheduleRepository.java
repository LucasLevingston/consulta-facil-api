package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.ProfessionalSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessionalScheduleRepository extends JpaRepository<ProfessionalSchedule, String> {
    List<ProfessionalSchedule> findByProfessionalId(String professionalId);
    Optional<ProfessionalSchedule> findByProfessionalIdAndDayOfWeek(String professionalId, String dayOfWeek);
}
