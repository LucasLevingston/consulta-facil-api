package com.example.consulta.api.controller;

import com.example.consulta.api.dto.appointment.PatientSummaryDTO;
import com.example.consulta.application.service.AppointmentService;
import com.example.consulta.application.service.PatientProfileService;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.core.security.SecurityUtils;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    private final PatientProfileService patientProfileService;
    private final AppointmentService appointmentService;
    private final ProfessionalProfileRepository professionalProfileRepository;

    @GetMapping("/professional/{userId}")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "List professional's patients", description = "Returns a paginated list of unique patients for a professional, with search and sort")
    public ResponseEntity<Page<PatientSummaryDTO>> getProfessionalPatients(
            @PathVariable String userId,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String professionalProfileId = professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional profile not found for user: " + userId))
                .getId();
        return ResponseEntity.ok(appointmentService.getProfessionalPatients(professionalProfileId, search, sort, page, size));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get my patient profile", description = "Returns the authenticated patient's profile")
    public ResponseEntity<?> getMyProfile() {
        String userId = SecurityUtils.getCurrentUserId();
        var profile = patientProfileService.getPatientProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get patient profile by user ID", description = "Returns a patient's profile by user ID")
    public ResponseEntity<?> getPatientProfile(@PathVariable String userId) {
        var profile = patientProfileService.getPatientProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Update my patient profile", description = "Updates the authenticated patient's profile")
    public ResponseEntity<?> updateMyProfile(@RequestBody Map<String, Object> updates) {
        String userId = SecurityUtils.getCurrentUserId();
        var profile = patientProfileService.updatePatientProfile(userId, updates);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{userId}/medical-records")
    @Operation(summary = "Get patient medical records", description = "Returns a patient's medical records")
    public ResponseEntity<?> getPatientMedicalRecords(@PathVariable String userId) {
        var records = patientProfileService.getPatientMedicalRecords(userId);
        return ResponseEntity.ok(records);
    }

    @PutMapping("/{userId}/medical-records")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Update patient medical records", description = "Updates a patient's medical records")
    public ResponseEntity<?> updatePatientMedicalRecords(@PathVariable String userId, @RequestBody Map<String, Object> updates) {
        var records = patientProfileService.updatePatientMedicalRecords(userId, updates);
        return ResponseEntity.ok(records);
    }
}
