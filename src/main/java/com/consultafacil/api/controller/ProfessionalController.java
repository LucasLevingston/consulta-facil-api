package com.consultafacil.api.controller;

import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdatePaymentSettingsDTO;
import com.consultafacil.api.dto.schedule.CreateProfessionalScheduleDTO;
import com.consultafacil.api.dto.schedule.ProfessionalScheduleResponseDTO;
import com.consultafacil.application.port.in.ProfessionalProfileUseCase;
import com.consultafacil.application.port.in.ProfessionalScheduleUseCase;
import com.consultafacil.application.port.in.SetConsultationPriceUseCase;
import com.consultafacil.application.port.in.UpdatePaymentSettingsUseCase;
import com.consultafacil.core.security.CustomUserDetails;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/professionals")
@RequiredArgsConstructor
@Tag(name = "Professionals", description = "Professional management endpoints")
public class ProfessionalController {

    private final ProfessionalProfileUseCase professionalProfileUseCase;
    private final ProfessionalScheduleUseCase professionalScheduleUseCase;
    private final SetConsultationPriceUseCase setConsultationPriceUseCase;
    private final UpdatePaymentSettingsUseCase updatePaymentSettingsUseCase;

    @GetMapping
    @Operation(summary = "List professionals", description = "Returns active professionals with optional filters")
    public ResponseEntity<Page<ProfessionalResponseDTO>> getAllProfessionals(
            @RequestParam(required = false) String profession,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(professionalProfileUseCase.getAll(profession, specialty, name, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search professionals by specialty")
    public ResponseEntity<Page<ProfessionalResponseDTO>> searchBySpecialty(
            @RequestParam String specialty, Pageable pageable) {
        return ResponseEntity.ok(professionalProfileUseCase.searchBySpecialty(specialty, pageable));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find professionals near a location (Haversine)")
    public ResponseEntity<List<ProfessionalResponseDTO>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double radiusKm,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String profession) {
        return ResponseEntity.ok(professionalProfileUseCase.getNearby(lat, lng, radiusKm, specialty, profession));
    }

    @GetMapping("/me")
    @PreAuthorize("@policy.canManageProfessionalSchedule(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my professional profile")
    public ResponseEntity<ProfessionalResponseDTO> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(professionalProfileUseCase.getByUserId(userDetails.getUserId()));
    }

    @GetMapping("/{professionalId}")
    @Operation(summary = "Get professional by ID")
    public ResponseEntity<ProfessionalResponseDTO> getProfessionalById(@PathVariable String professionalId) {
        return ResponseEntity.ok(professionalProfileUseCase.getById(professionalId));
    }

    @PostMapping
    @PreAuthorize("@policy.canCreateProfessionalProfile(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create professional profile")
    public ResponseEntity<ProfessionalResponseDTO> createProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateProfessionalDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(professionalProfileUseCase.createProfile(userDetails.getUserId(), dto));
    }

    @PutMapping("/{professionalId}")
    @PreAuthorize("@policy.canViewOwnProfessionalProfile(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update professional profile")
    public ResponseEntity<ProfessionalResponseDTO> updateProfessional(
            @PathVariable String professionalId,
            @Valid @RequestBody CreateProfessionalDTO dto) {
        return ResponseEntity.ok(professionalProfileUseCase.updateProfessional(professionalId, dto));
    }

    @DeleteMapping("/{professionalId}")
    @PreAuthorize("@policy.canAdminManageProfessional(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete professional profile")
    public ResponseEntity<Void> deleteProfessional(@PathVariable String professionalId) {
        professionalProfileUseCase.deleteProfessional(professionalId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/applications")
    @PreAuthorize("@policy.canAdminManageProfessional(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List pending professional applications (admin only)")
    public ResponseEntity<Page<ProfessionalResponseDTO>> getPendingApplications(Pageable pageable) {
        return ResponseEntity.ok(professionalProfileUseCase.getPendingApplications(pageable));
    }

    @GetMapping("/application-status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get own professional application status")
    public ResponseEntity<ProfessionalResponseDTO> getApplicationStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(professionalProfileUseCase.getApplicationStatus(userDetails.getUserId()));
    }

    @PutMapping("/{professionalId}/approve")
    @PreAuthorize("@policy.canAdminManageProfessional(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Approve a professional application (admin only)")
    public ResponseEntity<ProfessionalResponseDTO> approveApplication(@PathVariable String professionalId) {
        return ResponseEntity.ok(professionalProfileUseCase.approveApplication(professionalId));
    }

    @PutMapping("/{professionalId}/reject")
    @PreAuthorize("@policy.canAdminManageProfessional(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Reject a professional application (admin only)")
    public ResponseEntity<ProfessionalResponseDTO> rejectApplication(@PathVariable String professionalId) {
        return ResponseEntity.ok(professionalProfileUseCase.rejectApplication(professionalId));
    }

    @GetMapping("/{professionalId}/schedule")
    @Operation(summary = "Get schedule for a professional")
    public ResponseEntity<List<ProfessionalScheduleResponseDTO>> getSchedule(@PathVariable String professionalId) {
        return ResponseEntity.ok(professionalScheduleUseCase.getByProfessionalId(professionalId));
    }

    @GetMapping("/me/schedule")
    @PreAuthorize("@policy.canManageProfessionalSchedule(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my schedule")
    public ResponseEntity<List<ProfessionalScheduleResponseDTO>> getMySchedule(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(professionalScheduleUseCase.getMySchedule(userDetails.getUserId()));
    }

    @PutMapping("/me/consultation-price")
    @PreAuthorize("@policy.canManageProfessionalSchedule(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Set my consultation price")
    public ResponseEntity<ProfessionalResponseDTO> setConsultationPrice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, BigDecimal> body) {
        return ResponseEntity.ok(setConsultationPriceUseCase.execute(userDetails.getUserId(), body.get("price")));
    }

    @PutMapping("/me/payment-settings")
    @PreAuthorize("@policy.canManageProfessionalSchedule(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update my payment settings (accepted methods and timing)")
    public ResponseEntity<ProfessionalResponseDTO> updatePaymentSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdatePaymentSettingsDTO dto) {
        return ResponseEntity.ok(updatePaymentSettingsUseCase.execute(userDetails.getUserId(), dto));
    }

    @PutMapping("/me/schedule")
    @PreAuthorize("@policy.canManageProfessionalSchedule(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Save my weekly schedule (upsert)")
    public ResponseEntity<List<ProfessionalScheduleResponseDTO>> saveMySchedule(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody List<CreateProfessionalScheduleDTO> dtos) {
        return ResponseEntity.ok(professionalScheduleUseCase.saveMySchedule(userDetails.getUserId(), dtos));
    }
}
