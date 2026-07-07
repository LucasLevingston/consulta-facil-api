package com.consultafacil.api.controller;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateAddressDTO;
import com.consultafacil.api.dto.professional.UpdateCouncilDTO;
import com.consultafacil.application.port.in.UpdateAddressUseCase;
import com.consultafacil.application.port.in.UpdateCouncilUseCase;
import com.consultafacil.core.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/professionals/me")
@RequiredArgsConstructor
@Tag(name = "Professionals", description = "Professional council and address endpoints")
public class ProfessionalCouncilAddressController {

    private final UpdateCouncilUseCase updateCouncilUseCase;
    private final UpdateAddressUseCase updateAddressUseCase;

    @PatchMapping("/council")
    @PreAuthorize("@carePolicy.canUpdateOwnCouncil(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update my council registration")
    public ResponseEntity<ProfessionalResponseDTO> updateCouncil(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateCouncilDTO dto) {
        return ResponseEntity.ok(updateCouncilUseCase.execute(userDetails.getUserId(), dto));
    }

    @PatchMapping("/address")
    @PreAuthorize("@carePolicy.canUpdateOwnAddress(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update my address details")
    public ResponseEntity<ProfessionalResponseDTO> updateAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateAddressDTO dto) {
        return ResponseEntity.ok(updateAddressUseCase.execute(userDetails.getUserId(), dto));
    }
}
