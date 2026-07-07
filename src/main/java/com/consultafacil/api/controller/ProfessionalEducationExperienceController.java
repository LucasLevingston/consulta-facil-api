package com.consultafacil.api.controller;

import com.consultafacil.api.dto.professional.ProfessionalEducationDTO;
import com.consultafacil.api.dto.professional.ProfessionalExperienceDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.application.port.in.professional.enrichment.AddEducationUseCase;
import com.consultafacil.application.port.in.professional.enrichment.AddExperienceUseCase;
import com.consultafacil.application.port.in.professional.enrichment.DeleteEducationUseCase;
import com.consultafacil.application.port.in.professional.enrichment.DeleteExperienceUseCase;
import com.consultafacil.application.port.in.professional.enrichment.UpdateEducationUseCase;
import com.consultafacil.application.port.in.professional.enrichment.UpdateExperienceUseCase;
import com.consultafacil.core.security.CustomUserDetails;
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

@RestController
@RequestMapping("/professionals/me")
@RequiredArgsConstructor
@Tag(name = "Professionals", description = "Professional education and experience endpoints")
public class ProfessionalEducationExperienceController {

    private final AddEducationUseCase addEducationUseCase;
    private final UpdateEducationUseCase updateEducationUseCase;
    private final DeleteEducationUseCase deleteEducationUseCase;
    private final AddExperienceUseCase addExperienceUseCase;
    private final UpdateExperienceUseCase updateExperienceUseCase;
    private final DeleteExperienceUseCase deleteExperienceUseCase;

    @PostMapping("/education")
    @PreAuthorize("@carePolicy.canManageOwnEducation(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add education entry")
    public ResponseEntity<ProfessionalResponseDTO> addEducation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfessionalEducationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addEducationUseCase.execute(userDetails.getUserId(), dto));
    }

    @PutMapping("/education/{educationId}")
    @PreAuthorize("@carePolicy.canManageOwnEducation(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update education entry")
    public ResponseEntity<ProfessionalResponseDTO> updateEducation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String educationId,
            @Valid @RequestBody ProfessionalEducationDTO dto) {
        return ResponseEntity.ok(updateEducationUseCase.execute(userDetails.getUserId(), educationId, dto));
    }

    @DeleteMapping("/education/{educationId}")
    @PreAuthorize("@carePolicy.canManageOwnEducation(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete education entry")
    public ResponseEntity<ProfessionalResponseDTO> deleteEducation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String educationId) {
        return ResponseEntity.ok(deleteEducationUseCase.execute(userDetails.getUserId(), educationId));
    }

    @PostMapping("/experience")
    @PreAuthorize("@carePolicy.canManageOwnExperience(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add experience entry")
    public ResponseEntity<ProfessionalResponseDTO> addExperience(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfessionalExperienceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addExperienceUseCase.execute(userDetails.getUserId(), dto));
    }

    @PutMapping("/experience/{experienceId}")
    @PreAuthorize("@carePolicy.canManageOwnExperience(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update experience entry")
    public ResponseEntity<ProfessionalResponseDTO> updateExperience(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String experienceId,
            @Valid @RequestBody ProfessionalExperienceDTO dto) {
        return ResponseEntity.ok(updateExperienceUseCase.execute(userDetails.getUserId(), experienceId, dto));
    }

    @DeleteMapping("/experience/{experienceId}")
    @PreAuthorize("@carePolicy.canManageOwnExperience(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete experience entry")
    public ResponseEntity<ProfessionalResponseDTO> deleteExperience(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String experienceId) {
        return ResponseEntity.ok(deleteExperienceUseCase.execute(userDetails.getUserId(), experienceId));
    }
}
