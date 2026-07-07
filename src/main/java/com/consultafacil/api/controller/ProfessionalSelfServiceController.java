package com.consultafacil.api.controller;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateBioDTO;
import com.consultafacil.api.dto.professional.UpdatePaymentSettingsDTO;
import com.consultafacil.api.dto.professional.UpdateSocialLinksDTO;
import com.consultafacil.application.port.in.professional.profile.SetConsultationPriceUseCase;
import com.consultafacil.application.port.in.professional.profile.UpdateBioUseCase;
import com.consultafacil.application.port.in.billing.UpdatePaymentSettingsUseCase;
import com.consultafacil.application.port.in.professional.profile.UpdateSocialLinksUseCase;
import com.consultafacil.core.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/professionals/me")
@RequiredArgsConstructor
@Tag(name = "Professionals", description = "Professional self-service endpoints")
public class ProfessionalSelfServiceController {

    private final SetConsultationPriceUseCase setConsultationPriceUseCase;
    private final UpdatePaymentSettingsUseCase updatePaymentSettingsUseCase;
    private final UpdateBioUseCase updateBioUseCase;
    private final UpdateSocialLinksUseCase updateSocialLinksUseCase;

    @PutMapping("/consultation-price")
    @PreAuthorize("@carePolicy.canManageProfessionalSchedule(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Set my consultation price")
    public ResponseEntity<ProfessionalResponseDTO> setConsultationPrice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, BigDecimal> body) {
        return ResponseEntity.ok(setConsultationPriceUseCase.execute(userDetails.getUserId(), body.get("price")));
    }

    @PutMapping("/payment-settings")
    @PreAuthorize("@carePolicy.canManageProfessionalSchedule(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update my payment settings (accepted methods and timing)")
    public ResponseEntity<ProfessionalResponseDTO> updatePaymentSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdatePaymentSettingsDTO dto) {
        return ResponseEntity.ok(updatePaymentSettingsUseCase.execute(userDetails.getUserId(), dto));
    }

    @PatchMapping("/bio")
    @PreAuthorize("@carePolicy.canManageProfessionalSchedule(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update my bio")
    public ResponseEntity<ProfessionalResponseDTO> updateBio(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateBioDTO dto) {
        return ResponseEntity.ok(updateBioUseCase.execute(userDetails.getUserId(), dto));
    }

    @PatchMapping("/social-links")
    @PreAuthorize("@carePolicy.canManageProfessionalSchedule(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update my social media links")
    public ResponseEntity<ProfessionalResponseDTO> updateSocialLinks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateSocialLinksDTO dto) {
        return ResponseEntity.ok(updateSocialLinksUseCase.execute(userDetails.getUserId(), dto));
    }
}
