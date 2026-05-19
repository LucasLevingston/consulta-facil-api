package com.example.consulta.api.controller;

import com.example.consulta.api.dto.doctor.CreateDoctorDTO;
import com.example.consulta.api.dto.doctor.DoctorResponseDTO;
import com.example.consulta.application.service.DoctorService;
import com.example.consulta.core.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctors", description = "Doctor management endpoints")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    @Operation(summary = "List doctors", description = "Returns all doctors with pagination")
    public ResponseEntity<Page<DoctorResponseDTO>> getAllDoctors(Pageable pageable) {
        return ResponseEntity.ok(doctorService.getAllDoctors(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search doctors by specialty")
    public ResponseEntity<Page<DoctorResponseDTO>> searchBySpecialty(
            @RequestParam String specialty,
            Pageable pageable) {
        return ResponseEntity.ok(doctorService.searchDoctorsBySpecialty(specialty, pageable));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my doctor profile")
    public ResponseEntity<DoctorResponseDTO> getMyDoctorProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(doctorService.getDoctorByUserId(userDetails.getUserId()));
    }

    @GetMapping("/{doctorId}")
    @Operation(summary = "Get doctor by ID")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(@PathVariable String doctorId) {
        return ResponseEntity.ok(doctorService.getDoctorById(doctorId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create doctor profile", description = "Creates a doctor profile for the authenticated user")
    public ResponseEntity<DoctorResponseDTO> createDoctorProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateDoctorDTO dto) {
        DoctorResponseDTO response = doctorService.createDoctorProfile(userDetails.getUserId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update doctor profile")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(
            @PathVariable String doctorId,
            @Valid @RequestBody CreateDoctorDTO dto) {
        DoctorResponseDTO response = doctorService.updateDoctor(doctorId, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{doctorId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete doctor profile")
    public ResponseEntity<Void> deleteDoctor(@PathVariable String doctorId) {
        doctorService.deleteDoctor(doctorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/applications")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List pending doctor applications (admin only)")
    public ResponseEntity<Page<DoctorResponseDTO>> getPendingApplications(Pageable pageable) {
        return ResponseEntity.ok(doctorService.getPendingApplications(pageable));
    }

    @GetMapping("/application-status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get own doctor application status")
    public ResponseEntity<DoctorResponseDTO> getApplicationStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(doctorService.getApplicationStatus(userDetails.getUserId()));
    }

    @PutMapping("/{doctorId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Approve a doctor application (admin only)")
    public ResponseEntity<DoctorResponseDTO> approveDoctorApplication(@PathVariable String doctorId) {
        return ResponseEntity.ok(doctorService.approveDoctorApplication(doctorId));
    }

    @PutMapping("/{doctorId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Reject a doctor application (admin only)")
    public ResponseEntity<DoctorResponseDTO> rejectDoctorApplication(@PathVariable String doctorId) {
        return ResponseEntity.ok(doctorService.rejectDoctorApplication(doctorId));
    }
}
