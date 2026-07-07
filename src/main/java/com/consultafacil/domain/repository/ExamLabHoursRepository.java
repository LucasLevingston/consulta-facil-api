package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.ExamLabHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamLabHoursRepository extends JpaRepository<ExamLabHours, String> {

    List<ExamLabHours> findByExamLabId(String examLabId);

    Optional<ExamLabHours> findByExamLabIdAndDayOfWeek(String examLabId, String dayOfWeek);

    void deleteByExamLabId(String examLabId);
}
