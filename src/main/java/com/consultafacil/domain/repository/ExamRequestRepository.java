package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.ExamRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRequestRepository extends JpaRepository<ExamRequest, String> {

    List<ExamRequest> findByAppointmentId(String appointmentId);

    List<ExamRequest> findByPatientId(String patientId);
}
