package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.ExamScheduling;
import com.consultafacil.domain.enums.ExamSchedulingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExamSchedulingRepository extends JpaRepository<ExamScheduling, String> {

    List<ExamScheduling> findByExamLabIdAndScheduledDate(String examLabId, LocalDate scheduledDate);

    List<ExamScheduling> findByExamRequestId(String examRequestId);

    Optional<ExamScheduling> findByExamRequestIdAndStatus(String examRequestId, ExamSchedulingStatus status);
}
