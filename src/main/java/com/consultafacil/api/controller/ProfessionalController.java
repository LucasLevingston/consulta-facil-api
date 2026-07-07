package com.consultafacil.api.controller;

import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.application.port.in.professional.profile.CreateProfessionalProfileUseCase;
import com.consultafacil.application.port.in.professional.profile.DeleteProfessionalUseCase;
import com.consultafacil.application.port.in.professional.profile.GetAllProfessionalsUseCase;
import com.consultafacil.application.port.in.professional.profile.GetProfessionalByIdUseCase;
import com.consultafacil.application.port.in.professional.profile.GetProfessionalsNearbyUseCase;
import com.consultafacil.application.port.in.professional.profile.SearchProfessionalsBySpecialtyUseCase;
import com.consultafacil.application.port.in.professional.profile.UpdateProfessionalUseCase;
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

import java.util.List;

@RestController
@RequestMapping("/professionals")
@RequiredArgsConstructor
@Tag(name = "Professionals", description = "Professional management endpoints")
public class ProfessionalController {

    private final GetAllProfessionalsUseCase getAllProfessionals;
    private final SearchProfessionalsBySpecialtyUseCase searchProfessionalsBySpecialty;
    private final GetProfessionalsNearbyUseCase getProfessionalsNearby;
    private final GetProfessionalByIdUseCase getProfessionalById;
    private final CreateProfessionalProfileUseCase createProfessionalProfile;
    private final UpdateProfessionalUseCase updateProfessional;
    private final DeleteProfessionalUseCase deleteProfessional;

    @GetMapping
    @Operation(summary = "List professionals", description = "Returns active professionals with optional filters")
    public ResponseEntity<Page<ProfessionalResponseDTO>> getAllProfessionals(
            @RequestParam(required = false) String profession,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(getAllProfessionals.execute(profession, specialty, name, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search professionals by specialty")
    public ResponseEntity<Page<ProfessionalResponseDTO>> searchBySpecialty(
            @RequestParam String specialty, Pageable pageable) {
        return ResponseEntity.ok(searchProfessionalsBySpecialty.execute(specialty, pageable));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find professionals near a location (Haversine)")
    public ResponseEntity<List<ProfessionalResponseDTO>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double radiusKm,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String profession) {
        return ResponseEntity.ok(getProfessionalsNearby.execute(lat, lng, radiusKm, specialty, profession));
    }

    @GetMapping("/{professionalId}")
    @Operation(summary = "Get professional by ID")
    public ResponseEntity<ProfessionalResponseDTO> getProfessionalById(@PathVariable String professionalId) {
        return ResponseEntity.ok(getProfessionalById.execute(professionalId));
    }

    @PostMapping
    @PreAuthorize("@carePolicy.canCreateProfessionalProfile(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create professional profile")
    public ResponseEntity<ProfessionalResponseDTO> createProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateProfessionalDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createProfessionalProfile.execute(userDetails.getUserId(), dto));
    }

    @PutMapping("/{professionalId}")
    @PreAuthorize("@carePolicy.canViewOwnProfessionalProfile(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update professional profile")
    public ResponseEntity<ProfessionalResponseDTO> updateProfessional(
            @PathVariable String professionalId,
            @Valid @RequestBody CreateProfessionalDTO dto) {
        return ResponseEntity.ok(updateProfessional.execute(professionalId, dto));
    }

    @DeleteMapping("/{professionalId}")
    @PreAuthorize("@carePolicy.canAdminManageProfessional(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete professional profile")
    public ResponseEntity<Void> deleteProfessional(@PathVariable String professionalId) {
        deleteProfessional.execute(professionalId);
        return ResponseEntity.noContent().build();
    }
}
