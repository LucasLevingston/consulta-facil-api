package com.consultafacil.api.controller;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.schedule.CreateProfessionalScheduleDTO;
import com.consultafacil.api.dto.schedule.ProfessionalScheduleResponseDTO;
import com.consultafacil.application.port.in.GetMyScheduleUseCase;
import com.consultafacil.application.port.in.GetProfessionalByUserIdUseCase;
import com.consultafacil.application.port.in.GetProfessionalScheduleUseCase;
import com.consultafacil.application.port.in.SaveMyScheduleUseCase;
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

import java.util.List;

@RestController
@RequestMapping("/professionals")
@RequiredArgsConstructor
@Tag(name = "Professionals", description = "Professional schedule endpoints")
public class ProfessionalScheduleController {

    private final GetProfessionalScheduleUseCase getProfessionalSchedule;
    private final GetMyScheduleUseCase getMySchedule;
    private final SaveMyScheduleUseCase saveMySchedule;
    private final GetProfessionalByUserIdUseCase getProfessionalByUserId;

    @GetMapping("/me")
    @PreAuthorize("@carePolicy.canManageProfessionalSchedule(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my professional profile")
    public ResponseEntity<ProfessionalResponseDTO> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(getProfessionalByUserId.execute(userDetails.getUserId()));
    }

    @GetMapping("/{professionalId}/schedule")
    @Operation(summary = "Get schedule for a professional")
    public ResponseEntity<List<ProfessionalScheduleResponseDTO>> getSchedule(@PathVariable String professionalId) {
        return ResponseEntity.ok(getProfessionalSchedule.execute(professionalId));
    }

    @GetMapping("/me/schedule")
    @PreAuthorize("@carePolicy.canManageProfessionalSchedule(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my schedule")
    public ResponseEntity<List<ProfessionalScheduleResponseDTO>> getMySchedule(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(getMySchedule.execute(userDetails.getUserId()));
    }

    @PutMapping("/me/schedule")
    @PreAuthorize("@carePolicy.canManageProfessionalSchedule(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Save my weekly schedule (upsert)")
    public ResponseEntity<List<ProfessionalScheduleResponseDTO>> saveMySchedule(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody List<CreateProfessionalScheduleDTO> dtos) {
        return ResponseEntity.ok(saveMySchedule.execute(userDetails.getUserId(), dtos));
    }
}
