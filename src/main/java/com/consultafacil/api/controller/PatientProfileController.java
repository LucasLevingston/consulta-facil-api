package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.PatientSummaryDTO;
import com.consultafacil.application.port.in.AppointmentQueryUseCase;
import com.consultafacil.application.port.in.PatientProfileUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.SecurityUtils;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Patients", description = "Patient profile management endpoints")
public class PatientProfileController {

    private final PatientProfileUseCase patientProfileUseCase;
    private final AppointmentQueryUseCase appointmentQueryUseCase;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;

    @GetMapping("/professional/{userId}")
    @PreAuthorize("@policy.canViewPatientProfile(authentication)")
    @Operation(summary = "List professional's patients")
    public ResponseEntity<Page<PatientSummaryDTO>> getProfessionalPatients(
            @PathVariable String userId,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String professionalProfileId = professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional profile not found for user: " + userId))
                .getId();
        return ResponseEntity.ok(
                appointmentQueryUseCase.getProfessionalPatients(professionalProfileId, search, sort, page, size));
    }

    @GetMapping("/me")
    @PreAuthorize("@policy.canManagePatientProfile(authentication)")
    @Operation(summary = "Get my patient profile")
    public ResponseEntity<?> getMyProfile() {
        return ResponseEntity.ok(patientProfileUseCase.getPatientProfile(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get patient profile by user ID")
    public ResponseEntity<?> getPatientProfile(@PathVariable String userId) {
        return ResponseEntity.ok(patientProfileUseCase.getPatientProfile(userId));
    }

    @PutMapping("/me")
    @PreAuthorize("@policy.canManagePatientProfile(authentication)")
    @Operation(summary = "Update my patient profile")
    public ResponseEntity<?> updateMyProfile(@RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(
                patientProfileUseCase.updatePatientProfile(SecurityUtils.getCurrentUserId(), updates));
    }

    @GetMapping("/{userId}/medical-records")
    @Operation(summary = "Get patient medical records")
    public ResponseEntity<?> getPatientMedicalRecords(@PathVariable String userId) {
        return ResponseEntity.ok(patientProfileUseCase.getPatientMedicalRecords(userId));
    }

    @PutMapping("/{userId}/medical-records")
    @PreAuthorize("@policy.canManagePatientProfile(authentication)")
    @Operation(summary = "Update patient medical records")
    public ResponseEntity<?> updatePatientMedicalRecords(
            @PathVariable String userId, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(patientProfileUseCase.updatePatientMedicalRecords(userId, updates));
    }

    @GetMapping
    @PreAuthorize("@policy.canAdminListPatients(authentication)")
    @Operation(summary = "List all patients (admin)")
    public ResponseEntity<Page<Map<String, Object>>> getAllPatients(Pageable pageable) {
        return ResponseEntity.ok(patientProfileUseCase.getAllPatients(pageable));
    }
}
