package com.consultafacil.application.service.examrequest;

import com.consultafacil.api.dto.exam.ExamRequestResponseDTO;
import com.consultafacil.api.dto.exam.ReviewExamRequestDTO;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ExamRequest;
import com.consultafacil.domain.enums.ExamRequestStatus;
import com.consultafacil.domain.port.out.ExamRequestRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.application.port.in.ReviewExamUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewExamService implements ReviewExamUseCase {

    private final ExamRequestRepositoryPort examRequestRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;

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
