package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.ExamRequest;

import java.util.List;
import java.util.Optional;

public interface ExamRequestRepositoryPort {

    ExamRequest save(ExamRequest examRequest);

    Optional<ExamRequest> findById(String id);

    List<ExamRequest> findByAppointmentId(String appointmentId);

    List<ExamRequest> findByPatientId(String patientId);
}
