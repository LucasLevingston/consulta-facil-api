package com.example.consulta.application.service;

import com.example.consulta.api.dto.exam.ExamRequestResponseDTO;
import com.example.consulta.api.dto.exam.ReviewExamRequestDTO;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.ExamRequest;
import com.example.consulta.domain.enums.ExamRequestStatus;
import com.example.consulta.domain.repository.ExamRequestRepository;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
import com.example.consulta.application.port.in.ReviewExamUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewExamService implements ReviewExamUseCase {

    private final ExamRequestRepository examRequestRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;

    @Transactional
    public ExamRequestResponseDTO execute(String examId, String professionalUserId, ReviewExamRequestDTO dto) {
        ExamRequest examRequest = examRequestRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("ExamRequest", examId));

        var professional = professionalProfileRepository.findByUserId(professionalUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional profile not found for user: " + professionalUserId));

        if (!examRequest.getProfessional().getId().equals(professional.getId())) {
            throw new BadRequestException("You can only review exam requests assigned to you");
        }

        if (examRequest.getStatus() != ExamRequestStatus.UPLOADED) {
            throw new BadRequestException("Only uploaded exam requests can be reviewed. Current status: " + examRequest.getStatus());
        }

        examRequest.setProfessionalNotes(dto.getProfessionalNotes());
        examRequest.setStatus(ExamRequestStatus.REVIEWED);

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
