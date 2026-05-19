package com.example.consulta.api.controller;

import com.example.consulta.api.dto.clinic.ClinicResponseDTO;
import com.example.consulta.api.dto.clinic.CreateClinicDTO;
import com.example.consulta.application.service.ClinicService;
import com.example.consulta.application.service.NotificationService;
import com.example.consulta.core.security.CustomUserDetails;
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
@RequestMapping("/clinics")
@RequiredArgsConstructor
@Tag(name = "Clinics", description = "Clinic management endpoints")
public class ClinicController {

    private final ClinicService clinicService;
    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List all active clinics")
    public ResponseEntity<List<ClinicResponseDTO>> getAllClinics() {
        return ResponseEntity.ok(clinicService.getAllClinics());
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find clinics near a location")
    public ResponseEntity<List<ClinicResponseDTO>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double radiusKm) {
        return ResponseEntity.ok(clinicService.getClinicsNearby(lat, lng, radiusKm));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my clinic")
    public ResponseEntity<List<ClinicResponseDTO>> getMyClinic(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(clinicService.getMyClinic(userDetails.getUserId()));
    }

    @GetMapping("/{clinicId}")
    @Operation(summary = "Get clinic by ID")
    public ResponseEntity<ClinicResponseDTO> getClinicById(@PathVariable String clinicId) {
        return ResponseEntity.ok(clinicService.getClinicById(clinicId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a clinic")
    public ResponseEntity<ClinicResponseDTO> createClinic(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateClinicDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clinicService.createClinic(userDetails.getUserId(), dto));
    }

    @PutMapping("/{clinicId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a clinic")
    public ResponseEntity<ClinicResponseDTO> updateClinic(
            @PathVariable String clinicId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateClinicDTO dto) {
        return ResponseEntity.ok(clinicService.updateClinic(clinicId, userDetails.getUserId(), dto));
    }

    @PostMapping("/{clinicId}/members/{doctorProfileId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add a doctor to clinic")
    public ResponseEntity<Void> addMember(
            @PathVariable String clinicId,
            @PathVariable String doctorProfileId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        clinicService.addMember(clinicId, doctorProfileId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{clinicId}/members/{doctorProfileId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Remove a doctor from clinic")
    public ResponseEntity<Void> removeMember(
            @PathVariable String clinicId,
            @PathVariable String doctorProfileId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        clinicService.removeMember(clinicId, doctorProfileId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{clinicId}/invites/{doctorProfileId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Send a clinic invite to a doctor")
    public ResponseEntity<Void> inviteDoctor(
            @PathVariable String clinicId,
            @PathVariable String doctorProfileId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.sendClinicInvite(clinicId, doctorProfileId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
