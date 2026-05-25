package com.example.consulta.api.controller;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;
import com.example.consulta.api.dto.clinic.ClinicResponseDTO;
import com.example.consulta.api.dto.clinic.CreateClinicDTO;
import com.example.consulta.api.dto.receptionist.InviteReceptionistDTO;
import com.example.consulta.api.dto.receptionist.ReceptionistResponseDTO;
import com.example.consulta.api.dto.schedule.ClinicWorkingHoursResponseDTO;
import com.example.consulta.api.dto.schedule.CreateClinicWorkingHoursDTO;
import com.example.consulta.application.service.ClinicService;
import com.example.consulta.application.service.ClinicWorkingHoursService;
import com.example.consulta.application.service.GetClinicQueueService;
import com.example.consulta.application.service.GetClinicReceptionistsService;
import com.example.consulta.application.service.InviteReceptionistService;
import com.example.consulta.application.service.NotificationService;
import com.example.consulta.application.service.RemoveReceptionistService;
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
@RequestMapping("/clinics")
@RequiredArgsConstructor
@Tag(name = "Clinics", description = "Clinic management endpoints")
public class ClinicController {

    private final ClinicService clinicService;
    private final ClinicWorkingHoursService clinicWorkingHoursService;
    private final NotificationService notificationService;
    private final InviteReceptionistService inviteReceptionistService;
    private final RemoveReceptionistService removeReceptionistService;
    private final GetClinicReceptionistsService getClinicReceptionistsService;
    private final GetClinicQueueService getClinicQueueService;

    @GetMapping
    @Operation(summary = "List all active clinics")
    public ResponseEntity<List<ClinicResponseDTO>> getAllClinics() {
        return ResponseEntity.ok(clinicService.getAllClinics());
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find clinics near a location")
    public ResponseEntity<List<ClinicResponseDTO>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double radiusKm) {
        return ResponseEntity.ok(clinicService.getClinicsNearby(lat, lng, radiusKm));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my clinic")
    public ResponseEntity<List<ClinicResponseDTO>> getMyClinic(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(clinicService.getMyClinic(userDetails.getUserId()));
    }

    @GetMapping("/{clinicId}")
    @Operation(summary = "Get clinic by ID")
    public ResponseEntity<ClinicResponseDTO> getClinicById(@PathVariable String clinicId) {
        return ResponseEntity.ok(clinicService.getClinicById(clinicId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a clinic")
    public ResponseEntity<ClinicResponseDTO> createClinic(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateClinicDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clinicService.createClinic(userDetails.getUserId(), dto));
    }

    @PutMapping("/{clinicId}")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a clinic")
    public ResponseEntity<ClinicResponseDTO> updateClinic(
            @PathVariable String clinicId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateClinicDTO dto) {
        return ResponseEntity.ok(clinicService.updateClinic(clinicId, userDetails.getUserId(), dto));
    }

    @PostMapping("/{clinicId}/members/{professionalProfileId}")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add a professional to clinic")
    public ResponseEntity<Void> addMember(
            @PathVariable String clinicId,
            @PathVariable String professionalProfileId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        clinicService.addMember(clinicId, professionalProfileId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{clinicId}/members/{professionalProfileId}")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Remove a professional from clinic")
    public ResponseEntity<Void> removeMember(
            @PathVariable String clinicId,
            @PathVariable String professionalProfileId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        clinicService.removeMember(clinicId, professionalProfileId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{clinicId}/invites/{professionalProfileId}")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Send a clinic invite to a professional")
    public ResponseEntity<Void> inviteProfessional(
            @PathVariable String clinicId,
            @PathVariable String professionalProfileId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.sendClinicInvite(clinicId, professionalProfileId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{clinicId}/receptionists")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Invite a receptionist to a clinic")
    public ResponseEntity<ReceptionistResponseDTO> inviteReceptionist(
            @PathVariable String clinicId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody InviteReceptionistDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inviteReceptionistService.execute(clinicId, userDetails.getUserId(), dto));
    }

    @DeleteMapping("/{clinicId}/receptionists/{receptionistId}")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Remove a receptionist from a clinic")
    public ResponseEntity<Void> removeReceptionist(
            @PathVariable String clinicId,
            @PathVariable String receptionistId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        removeReceptionistService.execute(clinicId, receptionistId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{clinicId}/receptionists")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get receptionists of a clinic")
    public ResponseEntity<List<ReceptionistResponseDTO>> getReceptionists(
            @PathVariable String clinicId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(getClinicReceptionistsService.execute(clinicId, userDetails.getUserId()));
    }

    @GetMapping("/{clinicId}/queue")
    @Operation(summary = "Get today's waiting room queue for a clinic")
    public ResponseEntity<List<AppointmentResponseDTO>> getClinicQueue(@PathVariable String clinicId) {
        return ResponseEntity.ok(getClinicQueueService.execute(clinicId));
    }

    @GetMapping("/{clinicId}/working-hours")
    @Operation(summary = "Get working hours for a clinic")
    public ResponseEntity<List<ClinicWorkingHoursResponseDTO>> getWorkingHours(@PathVariable String clinicId) {
        return ResponseEntity.ok(clinicWorkingHoursService.getClinicWorkingHours(clinicId));
    }

    @PutMapping("/{clinicId}/working-hours")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Save working hours for a clinic (upsert, owner only)")
    public ResponseEntity<List<ClinicWorkingHoursResponseDTO>> saveWorkingHours(
            @PathVariable String clinicId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody List<CreateClinicWorkingHoursDTO> dtos) {
        return ResponseEntity.ok(clinicWorkingHoursService.saveClinicWorkingHours(
                clinicId, userDetails.getUserId(), dtos));
    }
}
