package com.example.consulta.api.controller;

import com.example.consulta.application.service.PatientProfileService;
import com.example.consulta.core.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Patients", description = "Patient profile management endpoints")
public class PatientProfileController {

    private final PatientProfileService patientProfileService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
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
    @PreAuthorize("hasRole('USER')")
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
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update patient medical records", description = "Updates a patient's medical records")
    public ResponseEntity<?> updatePatientMedicalRecords(@PathVariable String userId, @RequestBody Map<String, Object> updates) {
        var records = patientProfileService.updatePatientMedicalRecords(userId, updates);
        return ResponseEntity.ok(records);
    }
}
