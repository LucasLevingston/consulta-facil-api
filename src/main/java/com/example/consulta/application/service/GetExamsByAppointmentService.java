package com.example.consulta.application.service;

import com.example.consulta.api.dto.exam.ExamRequestResponseDTO;
import com.example.consulta.domain.repository.ExamRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetExamsByAppointmentService {

    private final ExamRequestRepository examRequestRepository;

    @Transactional(readOnly = true)
    public List<ExamRequestResponseDTO> execute(String appointmentId) {
        return examRequestRepository.findByAppointmentId(appointmentId).stream()
                .map(e -> ExamRequestResponseDTO.builder()
                        .id(e.getId())
                        .appointmentId(e.getAppointment().getId())
                        .professionalId(e.getProfessional().getId())
                        .professionalName(e.getProfessional().getUser().getName())
                        .patientId(e.getPatient().getId())
                        .patientName(e.getPatient().getUser().getName())
                        .examName(e.getExamName())
                        .instructions(e.getInstructions())
                        .status(e.getStatus())
                        .fileUrl(e.getFileUrl())
                        .fileName(e.getFileName())
                        .professionalNotes(e.getProfessionalNotes())
                        .createdAt(e.getCreatedAt())
                        .updatedAt(e.getUpdatedAt())
                        .build())
                .toList();
    }
}
