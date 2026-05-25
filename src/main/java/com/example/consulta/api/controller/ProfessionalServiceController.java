package com.example.consulta.api.controller;

import com.example.consulta.api.dto.professionalservice.CreateProfessionalServiceDTO;
import com.example.consulta.api.dto.professionalservice.ProfessionalServiceResponseDTO;
import com.example.consulta.api.dto.professionalservice.UpdateProfessionalServiceDTO;
import com.example.consulta.application.service.CreateProfessionalServiceService;
import com.example.consulta.application.service.DeactivateProfessionalServiceService;
import com.example.consulta.application.service.GetProfessionalServicesService;
import com.example.consulta.application.service.UpdateProfessionalServiceService;
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
@RequestMapping("/professional-services")
@RequiredArgsConstructor
@Tag(name = "Professional Services", description = "Catalog of services/procedures offered by professionals")
public class ProfessionalServiceController {

    private final CreateProfessionalServiceService createProfessionalServiceService;
    private final UpdateProfessionalServiceService updateProfessionalServiceService;
    private final DeactivateProfessionalServiceService deactivateProfessionalServiceService;
    private final GetProfessionalServicesService getProfessionalServicesService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a service/procedure in the professional's catalog")
    public ResponseEntity<ProfessionalServiceResponseDTO> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateProfessionalServiceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createProfessionalServiceService.execute(userDetails.getUserId(), dto));
    }

    @GetMapping("/{professionalId}")
    @Operation(summary = "List active services for a professional")
    public ResponseEntity<List<ProfessionalServiceResponseDTO>> listByProfessional(
            @PathVariable String professionalId) {
        return ResponseEntity.ok(getProfessionalServicesService.execute(professionalId));
    }

    @PutMapping("/{serviceId}")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a service in the catalog")
    public ResponseEntity<ProfessionalServiceResponseDTO> update(
            @PathVariable String serviceId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfessionalServiceDTO dto) {
        return ResponseEntity.ok(updateProfessionalServiceService.execute(serviceId, userDetails.getUserId(), dto));
    }

    @DeleteMapping("/{serviceId}")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Deactivate (soft-delete) a service")
    public ResponseEntity<Void> deactivate(
            @PathVariable String serviceId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        deactivateProfessionalServiceService.execute(serviceId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
