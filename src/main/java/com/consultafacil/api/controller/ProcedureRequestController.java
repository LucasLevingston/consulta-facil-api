package com.consultafacil.api.controller;

import com.consultafacil.api.dto.procedurerequest.CreateProcedureRequestDTO;
import com.consultafacil.api.dto.procedurerequest.ProcedureRequestResponseDTO;
import com.consultafacil.api.dto.procedurerequest.ScheduleProcedureRequestDTO;
import com.consultafacil.application.port.in.procedurerequest.CancelProcedureRequestUseCase;
import com.consultafacil.application.port.in.procedurerequest.CreateProcedureRequestUseCase;
import com.consultafacil.application.port.in.procedurerequest.GetPatientProcedureRequestsUseCase;
import com.consultafacil.application.port.in.procedurerequest.GetProfessionalProcedureRequestsUseCase;
import com.consultafacil.application.port.in.procedurerequest.ScheduleProcedureRequestUseCase;
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

import java.util.List;

@RestController
@RequestMapping("/procedure-requests")
@RequiredArgsConstructor
@Tag(name = "Procedure Requests", description = "Workflow for procedures requiring a prior consultation")
public class ProcedureRequestController {

    private final CreateProcedureRequestUseCase createProcedureRequest;
    private final GetPatientProcedureRequestsUseCase getPatientRequests;
    private final GetProfessionalProcedureRequestsUseCase getProfessionalRequests;
    private final ScheduleProcedureRequestUseCase scheduleProcedureRequest;
    private final CancelProcedureRequestUseCase cancelProcedureRequest;

    @PostMapping
    @PreAuthorize("@requestPolicy.canManageProcedureRequest(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Professional opens a procedure request for a patient")
    public ResponseEntity<ProcedureRequestResponseDTO> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateProcedureRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createProcedureRequest.execute(userDetails.getUserId(), dto));
    }

    @GetMapping("/mine")
    @PreAuthorize("@requestPolicy.canViewProcedureRequests(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List procedure requests for the authenticated user")
    public ResponseEntity<List<ProcedureRequestResponseDTO>> getMine(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isProfessional = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROFESSIONAL")
                        || a.getAuthority().equals("ROLE_ADMIN"));
        List<ProcedureRequestResponseDTO> result = isProfessional
                ? getProfessionalRequests.execute(userDetails.getUserId())
                : getPatientRequests.execute(userDetails.getUserId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{requestId}/schedule")
    @PreAuthorize("@requestPolicy.canReviewProcedureRequestAsPatient(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Patient schedules a time slot for an approved procedure request")
    public ResponseEntity<ProcedureRequestResponseDTO> schedule(
            @PathVariable String requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ScheduleProcedureRequestDTO dto) {
        return ResponseEntity.ok(scheduleProcedureRequest.execute(requestId, userDetails.getUserId(), dto));
    }

    @PutMapping("/{requestId}/cancel")
    @PreAuthorize("@requestPolicy.canViewProcedureRequest(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cancel a procedure request (patient or professional)")
    public ResponseEntity<ProcedureRequestResponseDTO> cancel(
            @PathVariable String requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(cancelProcedureRequest.execute(requestId, userDetails.getUserId()));
    }
}
