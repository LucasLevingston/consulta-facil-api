package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.ExamScheduling;
import com.consultafacil.domain.enums.ExamSchedulingStatus;
import com.consultafacil.domain.port.out.ExamSchedulingRepositoryPort;
import com.consultafacil.domain.repository.ExamSchedulingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExamSchedulingRepositoryAdapter implements ExamSchedulingRepositoryPort {

    private final ExamSchedulingRepository examSchedulingRepository;

    @Override
    public ExamScheduling save(ExamScheduling scheduling) {
        return examSchedulingRepository.save(scheduling);
    }

    @Override
    public Optional<ExamScheduling> findById(String id) {
        return examSchedulingRepository.findById(id);
    }

    @Override
    public List<ExamScheduling> findByExamLabIdAndScheduledDate(String examLabId, LocalDate date) {
        return examSchedulingRepository.findByExamLabIdAndScheduledDate(examLabId, date);
    }

    @Override
    public List<ExamScheduling> findByExamRequestId(String examRequestId) {
        return examSchedulingRepository.findByExamRequestId(examRequestId);
    }

    @Override
    public Optional<ExamScheduling> findByExamRequestIdAndStatus(String examRequestId, ExamSchedulingStatus status) {
        return examSchedulingRepository.findByExamRequestIdAndStatus(examRequestId, status);
    }
}
