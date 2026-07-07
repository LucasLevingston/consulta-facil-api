package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.ExamRequest;
import com.consultafacil.domain.enums.ExamRequestStatus;

import java.util.List;
import java.util.Optional;

public interface ExamRequestRepositoryPort {

    ExamRequest save(ExamRequest examRequest);

    Optional<ExamRequest> findById(String id);

    List<ExamRequest> findByAppointmentId(String appointmentId);

    List<ExamRequest> findByPatientId(String patientId);

    List<ExamRequest> findByPatientUserId(String userId);

    List<ExamRequest> findByPatientUserIdAndStatus(String userId, ExamRequestStatus status);

    List<ExamRequest> findByProfessionalUserId(String userId);

    List<ExamRequest> findByProfessionalUserIdAndStatus(String userId, ExamRequestStatus status);
}
