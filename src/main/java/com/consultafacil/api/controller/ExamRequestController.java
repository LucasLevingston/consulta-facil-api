package com.consultafacil.api.controller;

import com.consultafacil.api.dto.exam.CreateExamRequestDTO;
import com.consultafacil.api.dto.exam.ExamRequestResponseDTO;
import com.consultafacil.api.dto.exam.ReviewExamRequestDTO;
import com.consultafacil.application.port.in.GetExamsByAppointmentUseCase;
import com.consultafacil.application.port.in.GetMyExamsUseCase;
import com.consultafacil.application.port.in.RequestExamUseCase;
import com.consultafacil.application.port.in.ReviewExamUseCase;
import com.consultafacil.application.port.in.UploadExamUseCase;
import com.consultafacil.core.security.CustomUserDetails;
import com.consultafacil.domain.enums.ExamRequestStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Exam Requests", description = "Exam request management endpoints")
public class ExamRequestController {

    private final RequestExamUseCase requestExam;
    private final UploadExamUseCase uploadExam;
    private final ReviewExamUseCase reviewExam;
    private final GetExamsByAppointmentUseCase getExamsByAppointment;
    private final GetMyExamsUseCase getMyExams;

    @PostMapping("/appointments/{appointmentId}/exams")
    @PreAuthorize("@requestPolicy.canManageExamRequest(authentication)")
    @Operation(summary = "Request an exam for an appointment")
    public ResponseEntity<ExamRequestResponseDTO> requestExam(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateExamRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(requestExam.execute(appointmentId, userDetails.getUserId(), dto));
    }

    @GetMapping("/appointments/{appointmentId}/exams")
    @PreAuthorize("@requestPolicy.canViewExamRequests(authentication)")
    @Operation(summary = "List exam requests for an appointment")
    public ResponseEntity<List<ExamRequestResponseDTO>> getExamsByAppointment(
            @PathVariable String appointmentId) {
        return ResponseEntity.ok(getExamsByAppointment.execute(appointmentId));
    }

    @GetMapping("/exams/my")
    @PreAuthorize("@requestPolicy.canViewOwnExams(authentication)")
    @Operation(summary = "List all exam requests for the authenticated user")
    public ResponseEntity<List<ExamRequestResponseDTO>> getMyExams(
            @RequestParam(required = false) ExamRequestStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(getMyExams.execute(userDetails.getUserId(), status));
    }

    @PutMapping(value = "/exams/{examId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@requestPolicy.canReviewExamRequestAsPatient(authentication)")
    @Operation(summary = "Upload exam result file")
    public ResponseEntity<ExamRequestResponseDTO> uploadExam(
            @PathVariable String examId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(uploadExam.execute(examId, userDetails.getUserId(), file));
    }

    @PutMapping("/exams/{examId}/review")
    @PreAuthorize("@requestPolicy.canReviewExamRequestAsProfessional(authentication)")
    @Operation(summary = "Review an uploaded exam and add professional notes")
    public ResponseEntity<ExamRequestResponseDTO> reviewExam(
            @PathVariable String examId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewExamRequestDTO dto) {
        return ResponseEntity.ok(reviewExam.execute(examId, userDetails.getUserId(), dto));
    }
}
