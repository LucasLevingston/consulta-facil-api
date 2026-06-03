package com.example.consulta.api.controller;

import com.example.consulta.api.dto.exam.CreateExamRequestDTO;
import com.example.consulta.api.dto.exam.ExamRequestResponseDTO;
import com.example.consulta.api.dto.exam.ReviewExamRequestDTO;
import com.example.consulta.application.port.in.GetExamsByAppointmentUseCase;
import com.example.consulta.application.port.in.RequestExamUseCase;
import com.example.consulta.application.port.in.ReviewExamUseCase;
import com.example.consulta.application.port.in.UploadExamUseCase;
import com.example.consulta.core.security.CustomUserDetails;
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

    @PostMapping("/appointments/{appointmentId}/exams")
    @PreAuthorize("@policy.canManageExamRequest(authentication)")
    @Operation(summary = "Request an exam for an appointment")
    public ResponseEntity<ExamRequestResponseDTO> requestExam(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateExamRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(requestExam.execute(appointmentId, userDetails.getUserId(), dto));
    }

    @GetMapping("/appointments/{appointmentId}/exams")
    @PreAuthorize("@policy.canViewExamRequests(authentication)")
    @Operation(summary = "List exam requests for an appointment")
    public ResponseEntity<List<ExamRequestResponseDTO>> getExamsByAppointment(
            @PathVariable String appointmentId) {
        return ResponseEntity.ok(getExamsByAppointment.execute(appointmentId));
    }

    @PutMapping(value = "/exams/{examId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@policy.canReviewExamRequestAsPatient(authentication)")
    @Operation(summary = "Upload exam result file")
    public ResponseEntity<ExamRequestResponseDTO> uploadExam(
            @PathVariable String examId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(uploadExam.execute(examId, userDetails.getUserId(), file));
    }

    @PutMapping("/exams/{examId}/review")
    @PreAuthorize("@policy.canReviewExamRequestAsProfessional(authentication)")
    @Operation(summary = "Review an uploaded exam and add professional notes")
    public ResponseEntity<ExamRequestResponseDTO> reviewExam(
            @PathVariable String examId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewExamRequestDTO dto) {
        return ResponseEntity.ok(reviewExam.execute(examId, userDetails.getUserId(), dto));
    }
}
