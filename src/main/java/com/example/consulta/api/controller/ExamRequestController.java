package com.example.consulta.api.controller;

import com.example.consulta.api.dto.exam.CreateExamRequestDTO;
import com.example.consulta.api.dto.exam.ExamRequestResponseDTO;
import com.example.consulta.api.dto.exam.ReviewExamRequestDTO;
import com.example.consulta.application.service.GetExamsByAppointmentService;
import com.example.consulta.application.service.RequestExamService;
import com.example.consulta.application.service.ReviewExamService;
import com.example.consulta.application.service.UploadExamService;
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

    private final RequestExamService requestExamService;
    private final UploadExamService uploadExamService;
    private final ReviewExamService reviewExamService;
    private final GetExamsByAppointmentService getExamsByAppointmentService;

    @PostMapping("/appointments/{appointmentId}/exams")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "Request an exam for an appointment")
    public ResponseEntity<ExamRequestResponseDTO> requestExam(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateExamRequestDTO dto) {
        ExamRequestResponseDTO response = requestExamService.execute(
                appointmentId, userDetails.getUserId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/appointments/{appointmentId}/exams")
    @PreAuthorize("hasAnyRole('PATIENT', 'PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "List exam requests for an appointment")
    public ResponseEntity<List<ExamRequestResponseDTO>> getExamsByAppointment(
            @PathVariable String appointmentId) {
        return ResponseEntity.ok(getExamsByAppointmentService.execute(appointmentId));
    }

    @PutMapping(value = "/exams/{examId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Upload exam result file")
    public ResponseEntity<ExamRequestResponseDTO> uploadExam(
            @PathVariable String examId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("file") MultipartFile file) {
        ExamRequestResponseDTO response = uploadExamService.execute(
                examId, userDetails.getUserId(), file);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/exams/{examId}/review")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "Review an uploaded exam and add professional notes")
    public ResponseEntity<ExamRequestResponseDTO> reviewExam(
            @PathVariable String examId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewExamRequestDTO dto) {
        ExamRequestResponseDTO response = reviewExamService.execute(
                examId, userDetails.getUserId(), dto);
        return ResponseEntity.ok(response);
    }
}
