package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.api.dto.clinic.ClinicResponseDTO;
import com.consultafacil.api.dto.clinic.CreateClinicDTO;
import com.consultafacil.api.dto.receptionist.InviteReceptionistDTO;
import com.consultafacil.api.dto.receptionist.ReceptionistResponseDTO;
import com.consultafacil.api.dto.schedule.ClinicWorkingHoursResponseDTO;
import com.consultafacil.api.dto.schedule.CreateClinicWorkingHoursDTO;
import com.consultafacil.application.port.in.clinic.AddClinicMemberUseCase;
import com.consultafacil.application.port.in.clinic.ClinicWorkingHoursUseCase;
import com.consultafacil.application.port.in.clinic.CreateClinicUseCase;
import com.consultafacil.application.port.in.clinic.GetAllClinicsUseCase;
import com.consultafacil.application.port.in.clinic.GetClinicByIdUseCase;
import com.consultafacil.application.port.in.appointment.GetClinicQueueUseCase;
import com.consultafacil.application.port.in.clinic.GetClinicReceptionistsUseCase;
import com.consultafacil.application.port.in.clinic.GetClinicsNearbyUseCase;
import com.consultafacil.application.port.in.clinic.GetMyClinicUseCase;
import com.consultafacil.application.port.in.clinic.InviteReceptionistUseCase;
import com.consultafacil.application.port.in.clinic.RemoveClinicMemberUseCase;
import com.consultafacil.application.port.in.clinic.RemoveReceptionistUseCase;
import com.consultafacil.application.port.in.clinic.SendClinicInviteUseCase;
import com.consultafacil.application.port.in.clinic.UpdateClinicUseCase;
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
@RequestMapping("/clinics")
@RequiredArgsConstructor
@Tag(name = "Clinics", description = "Clinic management endpoints")
public class ClinicController {

    private final GetAllClinicsUseCase getAllClinicsUseCase;
    private final GetClinicsNearbyUseCase getClinicsNearbyUseCase;
    private final GetMyClinicUseCase getMyClinicUseCase;
    private final GetClinicByIdUseCase getClinicByIdUseCase;
    private final CreateClinicUseCase createClinicUseCase;
    private final UpdateClinicUseCase updateClinicUseCase;
    private final AddClinicMemberUseCase addClinicMemberUseCase;
    private final RemoveClinicMemberUseCase removeClinicMemberUseCase;
    private final SendClinicInviteUseCase sendClinicInviteUseCase;
    private final ClinicWorkingHoursUseCase clinicWorkingHoursUseCase;
    private final InviteReceptionistUseCase inviteReceptionistUseCase;
    private final RemoveReceptionistUseCase removeReceptionistUseCase;
    private final GetClinicReceptionistsUseCase getClinicReceptionistsUseCase;
    private final GetClinicQueueUseCase getClinicQueueUseCase;

    @GetMapping
    @Operation(summary = "List all active clinics")
    public ResponseEntity<List<ClinicResponseDTO>> getAllClinics() {
        return ResponseEntity.ok(getAllClinicsUseCase.execute());
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find clinics near a location")
    public ResponseEntity<List<ClinicResponseDTO>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double radiusKm) {
        return ResponseEntity.ok(getClinicsNearbyUseCase.execute(lat, lng, radiusKm));
    }

    @GetMapping("/my")
    @PreAuthorize("@carePolicy.canManageClinic(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my clinic")
    public ResponseEntity<List<ClinicResponseDTO>> getMyClinic(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(getMyClinicUseCase.execute(userDetails.getUserId()));
    }

    @GetMapping("/{clinicId}")
    @Operation(summary = "Get clinic by ID")
    public ResponseEntity<ClinicResponseDTO> getClinicById(@PathVariable String clinicId) {
        return ResponseEntity.ok(getClinicByIdUseCase.execute(clinicId));
    }

    @PostMapping
    @PreAuthorize("@carePolicy.canManageClinic(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a clinic")
    public ResponseEntity<ClinicResponseDTO> createClinic(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateClinicDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createClinicUseCase.execute(userDetails.getUserId(), dto));
    }

    @PutMapping("/{clinicId}")
    @PreAuthorize("@carePolicy.canManageClinic(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a clinic")
    public ResponseEntity<ClinicResponseDTO> updateClinic(
            @PathVariable String clinicId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateClinicDTO dto) {
        return ResponseEntity.ok(updateClinicUseCase.execute(clinicId, userDetails.getUserId(), dto));
    }

    @PostMapping("/{clinicId}/members/{professionalProfileId}")
    @PreAuthorize("@carePolicy.canManageClinic(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add a professional to clinic")
    public ResponseEntity<Void> addMember(
            @PathVariable String clinicId,
            @PathVariable String professionalProfileId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        addClinicMemberUseCase.execute(clinicId, professionalProfileId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{clinicId}/members/{professionalProfileId}")
    @PreAuthorize("@carePolicy.canManageClinic(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Remove a professional from clinic")
    public ResponseEntity<Void> removeMember(
            @PathVariable String clinicId,
            @PathVariable String professionalProfileId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        removeClinicMemberUseCase.execute(clinicId, professionalProfileId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{clinicId}/invites/{professionalProfileId}")
    @PreAuthorize("@carePolicy.canManageClinic(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Send a clinic invite to a professional")
    public ResponseEntity<Void> inviteProfessional(
            @PathVariable String clinicId,
            @PathVariable String professionalProfileId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        sendClinicInviteUseCase.execute(clinicId, professionalProfileId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{clinicId}/receptionists")
    @PreAuthorize("@carePolicy.canManageClinic(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Invite a receptionist to a clinic")
    public ResponseEntity<ReceptionistResponseDTO> inviteReceptionist(
            @PathVariable String clinicId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody InviteReceptionistDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inviteReceptionistUseCase.execute(clinicId, userDetails.getUserId(), dto));
    }

    @DeleteMapping("/{clinicId}/receptionists/{receptionistId}")
    @PreAuthorize("@carePolicy.canManageClinic(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Remove a receptionist from a clinic")
    public ResponseEntity<Void> removeReceptionist(
            @PathVariable String clinicId,
            @PathVariable String receptionistId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        removeReceptionistUseCase.execute(clinicId, receptionistId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{clinicId}/receptionists")
    @PreAuthorize("@carePolicy.canManageClinic(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get receptionists of a clinic")
    public ResponseEntity<List<ReceptionistResponseDTO>> getReceptionists(
            @PathVariable String clinicId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(getClinicReceptionistsUseCase.execute(clinicId, userDetails.getUserId()));
    }

    @GetMapping("/{clinicId}/queue")
    @Operation(summary = "Get today's waiting room queue for a clinic")
    public ResponseEntity<List<AppointmentResponseDTO>> getClinicQueue(@PathVariable String clinicId) {
        return ResponseEntity.ok(getClinicQueueUseCase.execute(clinicId));
    }

    @GetMapping("/{clinicId}/working-hours")
    @Operation(summary = "Get working hours for a clinic")
    public ResponseEntity<List<ClinicWorkingHoursResponseDTO>> getWorkingHours(@PathVariable String clinicId) {
        return ResponseEntity.ok(clinicWorkingHoursUseCase.getWorkingHours(clinicId));
    }

    @PutMapping("/{clinicId}/working-hours")
    @PreAuthorize("@carePolicy.canManageClinic(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Save working hours for a clinic (upsert, owner only)")
    public ResponseEntity<List<ClinicWorkingHoursResponseDTO>> saveWorkingHours(
            @PathVariable String clinicId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody List<CreateClinicWorkingHoursDTO> dtos) {
        return ResponseEntity.ok(clinicWorkingHoursUseCase.saveWorkingHours(clinicId, userDetails.getUserId(), dtos));
    }
}
