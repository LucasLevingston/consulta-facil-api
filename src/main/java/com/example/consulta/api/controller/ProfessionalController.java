package com.example.consulta.api.controller;

import com.example.consulta.api.dto.professional.CreateProfessionalDTO;
import com.example.consulta.api.dto.professional.ProfessionalResponseDTO;
import com.example.consulta.application.service.ProfessionalService;
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

import java.util.List;

@RestController
@RequestMapping("/professionals")
@RequiredArgsConstructor
@Tag(name = "Professionals", description = "Professional management endpoints")
public class ProfessionalController {

    private final ProfessionalService professionalService;

    @GetMapping
    @Operation(summary = "List professionals", description = "Returns active professionals with optional filters")
    public ResponseEntity<Page<ProfessionalResponseDTO>> getAllProfessionals(
            @RequestParam(required = false) String profession,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(professionalService.getAllProfessionals(profession, specialty, name, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search professionals by specialty")
    public ResponseEntity<Page<ProfessionalResponseDTO>> searchBySpecialty(
            @RequestParam String specialty,
            Pageable pageable) {
        return ResponseEntity.ok(professionalService.searchBySpecialty(specialty, pageable));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find professionals near a location (Haversine)")
    public ResponseEntity<List<ProfessionalResponseDTO>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double radiusKm,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String profession) {
        return ResponseEntity.ok(professionalService.getProfessionalsNearby(lat, lng, radiusKm, specialty, profession));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my professional profile")
    public ResponseEntity<ProfessionalResponseDTO> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(professionalService.getProfessionalByUserId(userDetails.getUserId()));
    }

    @GetMapping("/{professionalId}")
    @Operation(summary = "Get professional by ID")
    public ResponseEntity<ProfessionalResponseDTO> getProfessionalById(@PathVariable String professionalId) {
        return ResponseEntity.ok(professionalService.getProfessionalById(professionalId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create professional profile", description = "Creates a professional profile for the authenticated user")
    public ResponseEntity<ProfessionalResponseDTO> createProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateProfessionalDTO dto) {
        ProfessionalResponseDTO response = professionalService.createProfessionalProfile(userDetails.getUserId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{professionalId}")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update professional profile")
    public ResponseEntity<ProfessionalResponseDTO> updateProfessional(
            @PathVariable String professionalId,
            @Valid @RequestBody CreateProfessionalDTO dto) {
        ProfessionalResponseDTO response = professionalService.updateProfessional(professionalId, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{professionalId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete professional profile")
    public ResponseEntity<Void> deleteProfessional(@PathVariable String professionalId) {
        professionalService.deleteProfessional(professionalId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/applications")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List pending professional applications (admin only)")
    public ResponseEntity<Page<ProfessionalResponseDTO>> getPendingApplications(Pageable pageable) {
        return ResponseEntity.ok(professionalService.getPendingApplications(pageable));
    }

    @GetMapping("/application-status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get own professional application status")
    public ResponseEntity<ProfessionalResponseDTO> getApplicationStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(professionalService.getApplicationStatus(userDetails.getUserId()));
    }

    @PutMapping("/{professionalId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Approve a professional application (admin only)")
    public ResponseEntity<ProfessionalResponseDTO> approveApplication(@PathVariable String professionalId) {
        return ResponseEntity.ok(professionalService.approveApplication(professionalId));
    }

    @PutMapping("/{professionalId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Reject a professional application (admin only)")
    public ResponseEntity<ProfessionalResponseDTO> rejectApplication(@PathVariable String professionalId) {
        return ResponseEntity.ok(professionalService.rejectApplication(professionalId));
    }
}
