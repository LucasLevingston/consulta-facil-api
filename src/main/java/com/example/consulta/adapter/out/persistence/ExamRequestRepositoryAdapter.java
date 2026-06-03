package com.example.consulta.adapter.out.persistence;

import com.example.consulta.domain.entity.ExamRequest;
import com.example.consulta.domain.port.out.ExamRequestRepositoryPort;
import com.example.consulta.domain.repository.ExamRequestRepository;
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
}
