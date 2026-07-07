package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.ExamRequest;
import com.consultafacil.domain.enums.ExamRequestStatus;
import com.consultafacil.domain.port.out.ExamRequestRepositoryPort;
import com.consultafacil.domain.repository.ExamRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExamRequestRepositoryAdapter implements ExamRequestRepositoryPort {

    private final ExamRequestRepository examRequestRepository;

    @Override
    public ExamRequest save(ExamRequest examRequest) {
        return examRequestRepository.save(examRequest);
    }

    @Override
    public Optional<ExamRequest> findById(String id) {
        return examRequestRepository.findById(id);
    }

    @Override
    public List<ExamRequest> findByAppointmentId(String appointmentId) {
        return examRequestRepository.findByAppointmentId(appointmentId);
    }

    @Override
    public List<ExamRequest> findByPatientId(String patientId) {
        return examRequestRepository.findByPatientId(patientId);
    }

    @Override
    public List<ExamRequest> findByPatientUserId(String userId) {
        return examRequestRepository.findByPatient_User_Id(userId);
    }

    @Override
    public List<ExamRequest> findByPatientUserIdAndStatus(String userId, ExamRequestStatus status) {
        return examRequestRepository.findByPatient_User_IdAndStatus(userId, status);
    }

    @Override
    public List<ExamRequest> findByProfessionalUserId(String userId) {
        return examRequestRepository.findByProfessional_User_Id(userId);
    }

    @Override
    public List<ExamRequest> findByProfessionalUserIdAndStatus(String userId, ExamRequestStatus status) {
        return examRequestRepository.findByProfessional_User_IdAndStatus(userId, status);
    }
}
