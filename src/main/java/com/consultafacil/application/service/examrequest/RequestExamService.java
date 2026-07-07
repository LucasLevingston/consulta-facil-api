package com.consultafacil.application.service.examrequest;

import com.consultafacil.api.dto.exam.CreateExamRequestDTO;
import com.consultafacil.api.dto.exam.ExamRequestResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.ExamRequest;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.examrequest.ExamRequestRepositoryPort;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import com.consultafacil.application.port.in.examrequest.RequestExamUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RequestExamService implements RequestExamUseCase {

    private final AppointmentRepositoryPort appointmentRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ExamRequestRepositoryPort examRequestRepository;

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
