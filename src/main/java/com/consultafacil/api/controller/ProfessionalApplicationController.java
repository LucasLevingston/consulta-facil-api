package com.consultafacil.api.controller;

import com.consultafacil.api.dto.professional.ProfessionalRatingDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.application.port.in.AppointmentQueryUseCase;
import com.consultafacil.application.port.in.ProfessionalProfileUseCase;
import com.consultafacil.core.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/professionals")
@RequiredArgsConstructor
@Tag(name = "Professionals", description = "Professional application and rating endpoints")
public class ProfessionalApplicationController {

    private final ProfessionalProfileUseCase professionalProfileUseCase;
    private final AppointmentQueryUseCase appointmentQueryUseCase;

    @GetMapping("/applications")
    @PreAuthorize("@carePolicy.canAdminManageProfessional(authentication)")
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
    @PreAuthorize("@carePolicy.canAdminManageProfessional(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Approve a professional application (admin only)")
    public ResponseEntity<ProfessionalResponseDTO> approveApplication(@PathVariable String professionalId) {
        return ResponseEntity.ok(professionalProfileUseCase.approveApplication(professionalId));
    }

    @PutMapping("/{professionalId}/reject")
    @PreAuthorize("@carePolicy.canAdminManageProfessional(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Reject a professional application (admin only)")
    public ResponseEntity<ProfessionalResponseDTO> rejectApplication(@PathVariable String professionalId) {
        return ResponseEntity.ok(professionalProfileUseCase.rejectApplication(professionalId));
    }

    @GetMapping("/{professionalId}/ratings")
    @Operation(summary = "Get rating summary for a professional")
    public ResponseEntity<ProfessionalRatingDTO> getProfessionalRatings(@PathVariable String professionalId) {
        return ResponseEntity.ok(appointmentQueryUseCase.getProfessionalRatings(professionalId));
    }
}
