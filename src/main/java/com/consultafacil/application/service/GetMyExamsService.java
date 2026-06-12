package com.consultafacil.application.service;

import com.consultafacil.api.dto.exam.ExamRequestResponseDTO;
import com.consultafacil.application.port.in.GetMyExamsUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ExamRequest;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.ExamRequestStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.ExamRequestRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetMyExamsService implements GetMyExamsUseCase {

    private final ExamRequestRepositoryPort examRequestRepository;
    private final UserRepositoryPort userRepository;

    @Transactional(readOnly = true)
    @Override
    public List<ExamRequestResponseDTO> execute(String userId, ExamRequestStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        List<ExamRequest> exams = fetchExams(user, status);

        return exams.stream().map(this::toDTO).toList();
    }

    private List<ExamRequest> fetchExams(User user, ExamRequestStatus status) {
        if (user.getRole() == UserRole.PATIENT) {
            return status != null
                    ? examRequestRepository.findByPatientUserIdAndStatus(user.getId(), status)
                    : examRequestRepository.findByPatientUserId(user.getId());
        }
        return status != null
                ? examRequestRepository.findByProfessionalUserIdAndStatus(user.getId(), status)
                : examRequestRepository.findByProfessionalUserId(user.getId());
    }

    private ExamRequestResponseDTO toDTO(ExamRequest e) {
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
