package com.consultafacil.application.service;

import com.consultafacil.api.dto.exam.ExamRequestResponseDTO;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ExamRequest;
import com.consultafacil.domain.enums.ExamRequestStatus;
import com.consultafacil.domain.port.out.ExamRequestRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.StoragePort;
import com.consultafacil.application.port.in.UploadExamUseCase;
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
