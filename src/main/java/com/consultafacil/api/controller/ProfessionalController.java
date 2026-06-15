package com.consultafacil.api.controller;

import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.api.dto.professional.ProfessionalCertificateDTO;
import com.consultafacil.api.dto.professional.ProfessionalEducationDTO;
import com.consultafacil.api.dto.professional.ProfessionalExperienceDTO;
import com.consultafacil.api.dto.professional.ProfessionalRatingDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateAddressDTO;
import com.consultafacil.api.dto.professional.UpdateBioDTO;
import com.consultafacil.api.dto.professional.UpdateCouncilDTO;
import com.consultafacil.api.dto.professional.UpdatePaymentSettingsDTO;
import com.consultafacil.api.dto.professional.UpdateSocialLinksDTO;
import com.consultafacil.application.port.in.AppointmentQueryUseCase;
import com.consultafacil.api.dto.schedule.CreateProfessionalScheduleDTO;
import com.consultafacil.api.dto.schedule.ProfessionalScheduleResponseDTO;
import com.consultafacil.application.port.in.ProfessionalEnrichmentUseCase;
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
    private final AppointmentQueryUseCase appointmentQueryUseCase;
    private final ProfessionalEnrichmentUseCase professionalEnrichmentUseCase;

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

    @PatchMapping("/me/bio")
    @PreAuthorize("@policy.canManageProfessionalSchedule(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update my bio")
    public ResponseEntity<ProfessionalResponseDTO> updateBio(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateBioDTO dto) {
        return ResponseEntity.ok(professionalProfileUseCase.updateBio(userDetails.getUserId(), dto));
    }

    @PatchMapping("/me/social-links")
    @PreAuthorize("@policy.canManageProfessionalSchedule(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update my social media links")
    public ResponseEntity<ProfessionalResponseDTO> updateSocialLinks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateSocialLinksDTO dto) {
        return ResponseEntity.ok(professionalProfileUseCase.updateSocialLinks(userDetails.getUserId(), dto));
    }

    @GetMapping("/{professionalId}/ratings")
    @Operation(summary = "Get rating summary for a professional")
    public ResponseEntity<ProfessionalRatingDTO> getProfessionalRatings(
            @PathVariable String professionalId) {
        return ResponseEntity.ok(appointmentQueryUseCase.getProfessionalRatings(professionalId));
    }

    // ── Council ───────────────────────────────────────────────────────────

    @PatchMapping("/me/council")
    @PreAuthorize("@policy.canUpdateOwnCouncil(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update my council registration")
    public ResponseEntity<ProfessionalResponseDTO> updateCouncil(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateCouncilDTO dto) {
        return ResponseEntity.ok(professionalEnrichmentUseCase.updateCouncil(userDetails.getUserId(), dto));
    }

    // ── Address ───────────────────────────────────────────────────────────

    @PatchMapping("/me/address")
    @PreAuthorize("@policy.canUpdateOwnAddress(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update my address details")
    public ResponseEntity<ProfessionalResponseDTO> updateAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateAddressDTO dto) {
        return ResponseEntity.ok(professionalEnrichmentUseCase.updateAddress(userDetails.getUserId(), dto));
    }

    // ── Education ─────────────────────────────────────────────────────────

    @PostMapping("/me/education")
    @PreAuthorize("@policy.canManageOwnEducation(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add education entry")
    public ResponseEntity<ProfessionalResponseDTO> addEducation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfessionalEducationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(professionalEnrichmentUseCase.addEducation(userDetails.getUserId(), dto));
    }

    @PutMapping("/me/education/{educationId}")
    @PreAuthorize("@policy.canManageOwnEducation(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update education entry")
    public ResponseEntity<ProfessionalResponseDTO> updateEducation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String educationId,
            @Valid @RequestBody ProfessionalEducationDTO dto) {
        return ResponseEntity.ok(professionalEnrichmentUseCase.updateEducation(userDetails.getUserId(), educationId, dto));
    }

    @DeleteMapping("/me/education/{educationId}")
    @PreAuthorize("@policy.canManageOwnEducation(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete education entry")
    public ResponseEntity<ProfessionalResponseDTO> deleteEducation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String educationId) {
        return ResponseEntity.ok(professionalEnrichmentUseCase.deleteEducation(userDetails.getUserId(), educationId));
    }

    // ── Experience ────────────────────────────────────────────────────────

    @PostMapping("/me/experience")
    @PreAuthorize("@policy.canManageOwnExperience(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add experience entry")
    public ResponseEntity<ProfessionalResponseDTO> addExperience(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfessionalExperienceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(professionalEnrichmentUseCase.addExperience(userDetails.getUserId(), dto));
    }

    @PutMapping("/me/experience/{experienceId}")
    @PreAuthorize("@policy.canManageOwnExperience(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update experience entry")
    public ResponseEntity<ProfessionalResponseDTO> updateExperience(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String experienceId,
            @Valid @RequestBody ProfessionalExperienceDTO dto) {
        return ResponseEntity.ok(professionalEnrichmentUseCase.updateExperience(userDetails.getUserId(), experienceId, dto));
    }

    @DeleteMapping("/me/experience/{experienceId}")
    @PreAuthorize("@policy.canManageOwnExperience(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete experience entry")
    public ResponseEntity<ProfessionalResponseDTO> deleteExperience(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String experienceId) {
        return ResponseEntity.ok(professionalEnrichmentUseCase.deleteExperience(userDetails.getUserId(), experienceId));
    }

    // ── Certificates ──────────────────────────────────────────────────────

    @PostMapping("/me/certificates")
    @PreAuthorize("@policy.canManageOwnCertificate(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add certificate")
    public ResponseEntity<ProfessionalResponseDTO> addCertificate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfessionalCertificateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(professionalEnrichmentUseCase.addCertificate(userDetails.getUserId(), dto));
    }

    @PutMapping("/me/certificates/{certificateId}")
    @PreAuthorize("@policy.canManageOwnCertificate(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update certificate")
    public ResponseEntity<ProfessionalResponseDTO> updateCertificate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String certificateId,
            @Valid @RequestBody ProfessionalCertificateDTO dto) {
        return ResponseEntity.ok(professionalEnrichmentUseCase.updateCertificate(userDetails.getUserId(), certificateId, dto));
    }

    @DeleteMapping("/me/certificates/{certificateId}")
    @PreAuthorize("@policy.canManageOwnCertificate(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete certificate")
    public ResponseEntity<ProfessionalResponseDTO> deleteCertificate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String certificateId) {
        return ResponseEntity.ok(professionalEnrichmentUseCase.deleteCertificate(userDetails.getUserId(), certificateId));
    }
}
