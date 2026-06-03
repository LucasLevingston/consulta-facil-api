package com.example.consulta.application.service;

import com.example.consulta.api.dto.exam.ExamRequestResponseDTO;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.ExamRequest;
import com.example.consulta.domain.enums.ExamRequestStatus;
import com.example.consulta.domain.port.out.ExamRequestRepositoryPort;
import com.example.consulta.domain.port.out.PatientProfileRepositoryPort;
import com.example.consulta.domain.port.out.StoragePort;
import com.example.consulta.application.port.in.UploadExamUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UploadExamService implements UploadExamUseCase {

    private final ExamRequestRepositoryPort examRequestRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;
    private final StoragePort storagePort;

    @Transactional
    public ExamRequestResponseDTO execute(String examId, String patientUserId, MultipartFile file) {
        ExamRequest examRequest = examRequestRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("ExamRequest", examId));

        var patient = patientProfileRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + patientUserId));

        if (!examRequest.getPatient().getId().equals(patient.getId())) {
            throw new BadRequestException("You can only upload files for your own exam requests");
        }

        if (examRequest.getStatus() == ExamRequestStatus.REVIEWED) {
            throw new BadRequestException("Cannot upload file for an already reviewed exam request");
        }

        String fileUrl;
        try {
            fileUrl = storagePort.upload(file.getBytes(), file.getOriginalFilename(), file.getContentType(), "exams");
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to upload exam file", e);
        }

        examRequest.setFileUrl(fileUrl);
        examRequest.setFileName(file.getOriginalFilename());
        examRequest.setStatus(ExamRequestStatus.UPLOADED);

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
