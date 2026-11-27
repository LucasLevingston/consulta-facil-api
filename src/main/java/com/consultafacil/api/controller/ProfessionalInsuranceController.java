package com.consultafacil.api.controller;

import com.consultafacil.api.dto.insurance.AddProfessionalInsuranceRequest;
import com.consultafacil.api.dto.insurance.ProfessionalInsuranceDTO;
import com.consultafacil.application.port.in.insurance.AddProfessionalInsuranceUseCase;
import com.consultafacil.application.port.in.insurance.GetProfessionalInsuranceUseCase;
import com.consultafacil.application.port.in.insurance.RemoveProfessionalInsuranceUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.SecurityUtils;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/professionals/me/insurance")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Professional Insurance", description = "Manage health insurance plans accepted by the professional")
public class ProfessionalInsuranceController {

    private final AddProfessionalInsuranceUseCase addProfessionalInsurance;
    private final RemoveProfessionalInsuranceUseCase removeProfessionalInsurance;
    private final GetProfessionalInsuranceUseCase getProfessionalInsurance;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;

    @PostMapping
    @PreAuthorize("@carePolicy.canManageProfessionalInsurance(authentication)")
    @Operation(summary = "Add an accepted insurance plan")
    public ResponseEntity<Void> addInsurance(@Valid @RequestBody AddProfessionalInsuranceRequest request) {
        String professionalId = resolveProfileId();
        addProfessionalInsurance.execute(professionalId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{planId}")
    @PreAuthorize("@carePolicy.canManageProfessionalInsurance(authentication)")
    @Operation(summary = "Remove an accepted insurance plan")
    public ResponseEntity<Void> removeInsurance(@PathVariable String planId) {
        String professionalId = resolveProfileId();
        removeProfessionalInsurance.execute(professionalId, planId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("@carePolicy.canManageProfessionalInsurance(authentication)")
    @Operation(summary = "List accepted insurance plans for the current professional")
    public ResponseEntity<List<ProfessionalInsuranceDTO>> listInsurance() {
        String professionalId = resolveProfileId();
        return ResponseEntity.ok(getProfessionalInsurance.execute(professionalId));
    }

    private String resolveProfileId() {
        String userId = SecurityUtils.getCurrentUserId();
        return professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional profile not found for user: " + userId))
                .getId();
    }
}
