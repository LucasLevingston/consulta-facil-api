package com.consultafacil.domain.repository.examrequest;

import com.consultafacil.domain.entity.ExamRequest;
import com.consultafacil.domain.enums.ExamRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRequestRepository extends JpaRepository<ExamRequest, String> {

    List<ExamRequest> findByAppointmentId(String appointmentId);

    List<ExamRequest> findByPatientId(String patientId);

    List<ExamRequest> findByPatient_User_Id(String userId);

    List<ExamRequest> findByPatient_User_IdAndStatus(String userId, ExamRequestStatus status);

    List<ExamRequest> findByProfessional_User_Id(String userId);

    List<ExamRequest> findByProfessional_User_IdAndStatus(String userId, ExamRequestStatus status);
}
