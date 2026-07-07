package com.consultafacil.api.controller;

import com.consultafacil.api.dto.professionalservice.CreateProfessionalServiceDTO;
import com.consultafacil.api.dto.professionalservice.ProfessionalServiceResponseDTO;
import com.consultafacil.api.dto.professionalservice.UpdateProfessionalServiceDTO;
import com.consultafacil.application.port.in.professional.service.CreateProfessionalServiceUseCase;
import com.consultafacil.application.port.in.professional.service.DeactivateProfessionalServiceUseCase;
import com.consultafacil.application.port.in.professional.service.GetProfessionalServicesUseCase;
import com.consultafacil.application.port.in.professional.service.UpdateProfessionalServiceUseCase;
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

import java.util.List;

@RestController
@RequestMapping("/professional-services")
@RequiredArgsConstructor
@Tag(name = "Professional Services", description = "Catalog of services/procedures offered by professionals")
public class ProfessionalServiceController {

    private final CreateProfessionalServiceUseCase createProfessionalService;
    private final UpdateProfessionalServiceUseCase updateProfessionalService;
    private final DeactivateProfessionalServiceUseCase deactivateProfessionalService;
    private final GetProfessionalServicesUseCase getProfessionalServices;

    @PostMapping
    @PreAuthorize("@carePolicy.canManageProfessionalService(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a service/procedure in the professional's catalog")
    public ResponseEntity<ProfessionalServiceResponseDTO> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateProfessionalServiceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createProfessionalService.execute(userDetails.getUserId(), dto));
    }

    @GetMapping("/{professionalId}")
    @Operation(summary = "List active services for a professional")
    public ResponseEntity<List<ProfessionalServiceResponseDTO>> listByProfessional(
            @PathVariable String professionalId) {
        return ResponseEntity.ok(getProfessionalServices.execute(professionalId));
    }

    @PutMapping("/{serviceId}")
    @PreAuthorize("@carePolicy.canManageProfessionalService(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a service in the catalog")
    public ResponseEntity<ProfessionalServiceResponseDTO> update(
            @PathVariable String serviceId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfessionalServiceDTO dto) {
        return ResponseEntity.ok(updateProfessionalService.execute(serviceId, userDetails.getUserId(), dto));
    }

    @DeleteMapping("/{serviceId}")
    @PreAuthorize("@carePolicy.canManageProfessionalService(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Deactivate (soft-delete) a service")
    public ResponseEntity<Void> deactivate(
            @PathVariable String serviceId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        deactivateProfessionalService.execute(serviceId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
