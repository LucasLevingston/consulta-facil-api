package com.consultafacil.domain.port.out.examlab;

import com.consultafacil.domain.entity.ExamLabHours;

import java.util.List;
import java.util.Optional;

public interface ExamLabHoursRepositoryPort {

    ExamLabHours save(ExamLabHours hours);

    List<ExamLabHours> findByExamLabId(String examLabId);

    Optional<ExamLabHours> findByExamLabIdAndDayOfWeek(String examLabId, String dayOfWeek);

    void deleteByExamLabId(String examLabId);
}
