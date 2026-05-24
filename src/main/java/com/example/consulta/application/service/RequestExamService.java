package com.example.consulta.application.service;

import com.example.consulta.api.dto.exam.CreateExamRequestDTO;
import com.example.consulta.api.dto.exam.ExamRequestResponseDTO;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.entity.ExamRequest;
import com.example.consulta.domain.repository.AppointmentRepository;
import com.example.consulta.domain.repository.ExamRequestRepository;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RequestExamService {

    private final AppointmentRepository appointmentRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final ExamRequestRepository examRequestRepository;

    @Transactional
    public ExamRequestResponseDTO execute(String appointmentId, String professionalUserId, CreateExamRequestDTO dto) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        var professional = professionalProfileRepository.findByUserId(professionalUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional profile not found for user: " + professionalUserId));

        ExamRequest examRequest = ExamRequest.builder()
                .appointment(appointment)
                .professional(professional)
                .patient(appointment.getPatient())
                .examName(dto.getExamName())
                .instructions(dto.getInstructions())
                .build();

        ExamRequest saved = examRequestRepository.save(examRequest);
        return toResponseDTO(saved);
    }

    private ExamRequestResponseDTO toResponseDTO(ExamRequest e) {
        return ExamRequestResponseDTO.builder()
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
                .build();
    }
}
