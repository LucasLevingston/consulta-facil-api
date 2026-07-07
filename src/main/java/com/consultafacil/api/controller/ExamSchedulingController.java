package com.consultafacil.api.controller;

import com.consultafacil.api.dto.examscheduling.ExamSchedulingResponseDTO;
import com.consultafacil.api.dto.examscheduling.ScheduleExamDTO;
import com.consultafacil.application.port.in.examrequest.CancelExamSchedulingUseCase;
import com.consultafacil.application.port.in.examrequest.ScheduleExamUseCase;
import com.consultafacil.core.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/exam-schedulings")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Exam Schedulings", description = "Schedule exams at labs")
public class ExamSchedulingController {

    private final ScheduleExamUseCase scheduleExam;
    private final CancelExamSchedulingUseCase cancelExamScheduling;

    @PostMapping
    @PreAuthorize("@requestPolicy.canScheduleExamAtLab(authentication)")
    @Operation(summary = "Schedule an exam at a lab")
    public ResponseEntity<ExamSchedulingResponseDTO> schedule(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ScheduleExamDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scheduleExam.execute(userDetails.getUserId(), dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@requestPolicy.canScheduleExamAtLab(authentication)")
    @Operation(summary = "Cancel an exam scheduling")
    public ResponseEntity<Void> cancel(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        cancelExamScheduling.execute(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
