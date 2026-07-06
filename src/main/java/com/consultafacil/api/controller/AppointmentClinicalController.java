package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.ClinicalNoteResponseDTO;
import com.consultafacil.api.dto.appointment.MedicalHistoryResponseDTO;
import com.consultafacil.api.dto.appointment.SaveClinicalNoteDTO;
import com.consultafacil.api.dto.appointment.SaveMedicalHistoryDTO;
import com.consultafacil.application.port.in.GetClinicalNoteUseCase;
import com.consultafacil.application.port.in.GetMedicalHistoryUseCase;
import com.consultafacil.application.port.in.SaveClinicalNoteUseCase;
import com.consultafacil.application.port.in.SaveMedicalHistoryUseCase;
import com.consultafacil.core.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Appointments", description = "Appointment anamnesis and clinical note endpoints")
public class AppointmentClinicalController {

    private final GetMedicalHistoryUseCase getMedicalHistoryUseCase;
    private final SaveMedicalHistoryUseCase saveMedicalHistoryUseCase;
    private final GetClinicalNoteUseCase getClinicalNoteUseCase;
    private final SaveClinicalNoteUseCase saveClinicalNoteUseCase;

    @GetMapping("/{appointmentId}/anamnesis")
    @PreAuthorize("@appointmentPolicy.canViewAnamnesis(authentication)")
    @Operation(summary = "Get anamnesis for an appointment")
    public ResponseEntity<MedicalHistoryResponseDTO> getAnamnesis(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return getMedicalHistoryUseCase.execute(appointmentId, userDetails.getUserId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/{appointmentId}/anamnesis")
    @PreAuthorize("@appointmentPolicy.canSaveAnamnesis(authentication)")
    @Operation(summary = "Save anamnesis for an appointment")
    public ResponseEntity<MedicalHistoryResponseDTO> saveAnamnesis(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SaveMedicalHistoryDTO dto) {
        return ResponseEntity.ok(saveMedicalHistoryUseCase.execute(appointmentId, userDetails.getUserId(), dto));
    }

    @GetMapping("/{appointmentId}/clinicalNote")
    @PreAuthorize("@appointmentPolicy.canViewClinicalNote(authentication)")
    @Operation(summary = "Get clinical note for an appointment")
    public ResponseEntity<ClinicalNoteResponseDTO> getClinicalNote(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return getClinicalNoteUseCase.execute(appointmentId, userDetails.getUserId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/{appointmentId}/clinicalNote")
    @PreAuthorize("@appointmentPolicy.canSaveClinicalNote(authentication)")
    @Operation(summary = "Save clinicalNote for an appointment")
    public ResponseEntity<ClinicalNoteResponseDTO> saveClinicalNote(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SaveClinicalNoteDTO dto) {
        return ResponseEntity.ok(saveClinicalNoteUseCase.execute(appointmentId, userDetails.getUserId(), dto));
    }
}
