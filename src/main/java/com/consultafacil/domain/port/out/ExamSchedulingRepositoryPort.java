package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.ExamScheduling;
import com.consultafacil.domain.enums.ExamSchedulingStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExamSchedulingRepositoryPort {

    ExamScheduling save(ExamScheduling scheduling);

    Optional<ExamScheduling> findById(String id);

    List<ExamScheduling> findByExamLabIdAndScheduledDate(String examLabId, LocalDate date);

    List<ExamScheduling> findByExamRequestId(String examRequestId);

    Optional<ExamScheduling> findByExamRequestIdAndStatus(String examRequestId, ExamSchedulingStatus status);
}
