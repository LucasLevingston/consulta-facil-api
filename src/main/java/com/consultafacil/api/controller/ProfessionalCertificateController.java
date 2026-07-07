package com.consultafacil.api.controller;

import com.consultafacil.api.dto.professional.ProfessionalCertificateDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.application.port.in.AddCertificateUseCase;
import com.consultafacil.application.port.in.DeleteCertificateUseCase;
import com.consultafacil.application.port.in.UpdateCertificateUseCase;
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
@RequestMapping("/professionals/me/certificates")
@RequiredArgsConstructor
@Tag(name = "Professionals", description = "Professional certificate endpoints")
public class ProfessionalCertificateController {

    private final AddCertificateUseCase addCertificateUseCase;
    private final UpdateCertificateUseCase updateCertificateUseCase;
    private final DeleteCertificateUseCase deleteCertificateUseCase;

    @PostMapping
    @PreAuthorize("@carePolicy.canManageOwnCertificate(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add certificate")
    public ResponseEntity<ProfessionalResponseDTO> addCertificate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfessionalCertificateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addCertificateUseCase.execute(userDetails.getUserId(), dto));
    }

    @PutMapping("/{certificateId}")
    @PreAuthorize("@carePolicy.canManageOwnCertificate(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update certificate")
    public ResponseEntity<ProfessionalResponseDTO> updateCertificate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String certificateId,
            @Valid @RequestBody ProfessionalCertificateDTO dto) {
        return ResponseEntity.ok(updateCertificateUseCase.execute(userDetails.getUserId(), certificateId, dto));
    }

    @DeleteMapping("/{certificateId}")
    @PreAuthorize("@carePolicy.canManageOwnCertificate(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete certificate")
    public ResponseEntity<ProfessionalResponseDTO> deleteCertificate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String certificateId) {
        return ResponseEntity.ok(deleteCertificateUseCase.execute(userDetails.getUserId(), certificateId));
    }
}
