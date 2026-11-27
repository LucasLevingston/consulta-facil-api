package com.consultafacil.api.controller;

import com.consultafacil.api.dto.insurance.PatientInsuranceDTO;
import com.consultafacil.api.dto.insurance.RegisterPatientInsuranceRequest;
import com.consultafacil.application.port.in.insurance.GetPatientInsuranceUseCase;
import com.consultafacil.application.port.in.insurance.RegisterPatientInsuranceUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.SecurityUtils;
import com.consultafacil.domain.port.out.patient.PatientProfileRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients/me/insurance")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Patient Insurance", description = "Manage health insurance for the current patient")
public class PatientInsuranceController {

    private final RegisterPatientInsuranceUseCase registerPatientInsurance;
    private final GetPatientInsuranceUseCase getPatientInsurance;
    private final PatientProfileRepositoryPort patientProfileRepository;

    @PostMapping
    @PreAuthorize("@carePolicy.canManageOwnInsurance(authentication)")
    @Operation(summary = "Register a health insurance plan for the current patient")
    public ResponseEntity<PatientInsuranceDTO> registerInsurance(
            @Valid @RequestBody RegisterPatientInsuranceRequest request) {
        String patientId = resolvePatientProfileId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registerPatientInsurance.execute(patientId, request));
    }

    @GetMapping
    @PreAuthorize("@carePolicy.canManageOwnInsurance(authentication)")
    @Operation(summary = "List health insurance plans for the current patient")
    public ResponseEntity<List<PatientInsuranceDTO>> listInsurance() {
        String patientId = resolvePatientProfileId();
        return ResponseEntity.ok(getPatientInsurance.execute(patientId));
    }

    private String resolvePatientProfileId() {
        String userId = SecurityUtils.getCurrentUserId();
        return patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + userId))
                .getId();
    }
}
