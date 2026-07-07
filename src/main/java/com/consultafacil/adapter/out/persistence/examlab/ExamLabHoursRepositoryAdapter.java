package com.consultafacil.adapter.out.persistence.examlab;

import com.consultafacil.domain.entity.ExamLabHours;
import com.consultafacil.domain.port.out.examlab.ExamLabHoursRepositoryPort;
import com.consultafacil.domain.repository.examlab.ExamLabHoursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExamLabHoursRepositoryAdapter implements ExamLabHoursRepositoryPort {

    private final ExamLabHoursRepository examLabHoursRepository;

    @Override
    public ExamLabHours save(ExamLabHours hours) {
        return examLabHoursRepository.save(hours);
    }

    @Override
    public List<ExamLabHours> findByExamLabId(String examLabId) {
        return examLabHoursRepository.findByExamLabId(examLabId);
    }

    @Override
    public Optional<ExamLabHours> findByExamLabIdAndDayOfWeek(String examLabId, String dayOfWeek) {
        return examLabHoursRepository.findByExamLabIdAndDayOfWeek(examLabId, dayOfWeek);
    }

    @Override
    public void deleteByExamLabId(String examLabId) {
        examLabHoursRepository.deleteByExamLabId(examLabId);
    }
}
