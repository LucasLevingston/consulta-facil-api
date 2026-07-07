package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.api.dto.appointment.QrCheckInTokenDTO;
import com.consultafacil.api.dto.appointment.SetModalityDTO;
import com.consultafacil.application.port.in.appointment.CallNextPatientUseCase;
import com.consultafacil.application.port.in.appointment.CheckInByQrUseCase;
import com.consultafacil.application.port.in.appointment.GenerateCheckInTokenUseCase;
import com.consultafacil.application.port.in.appointment.GenerateMeetLinkUseCase;
import com.consultafacil.application.port.in.appointment.GetQueueUseCase;
import com.consultafacil.application.port.in.appointment.SetAppointmentModalityUseCase;
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
@RequestMapping("/appointments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Appointments", description = "Appointment queue and check-in endpoints")
public class AppointmentQueueController {

    private final GenerateCheckInTokenUseCase generateCheckInToken;
    private final CheckInByQrUseCase checkInByQr;
    private final GetQueueUseCase getQueue;
    private final CallNextPatientUseCase callNextPatient;
    private final SetAppointmentModalityUseCase setModality;
    private final GenerateMeetLinkUseCase generateMeetLink;

    @GetMapping("/{appointmentId}/checkin-token")
    @PreAuthorize("@appointmentPolicy.canGenerateCheckInToken(authentication)")
    @Operation(summary = "Generate QR check-in token for patient")
    public ResponseEntity<QrCheckInTokenDTO> generateCheckInToken(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(generateCheckInToken.execute(appointmentId, userDetails.getUserId()));
    }

    @PostMapping("/checkin")
    @PreAuthorize("@appointmentPolicy.canCheckIn(authentication)")
    @Operation(summary = "Check in patient via QR token")
    public ResponseEntity<AppointmentResponseDTO> checkInByQr(@RequestParam String token) {
        return ResponseEntity.ok(checkInByQr.execute(token));
    }

    @GetMapping("/queue")
    @PreAuthorize("@appointmentPolicy.canViewQueue(authentication)")
    @Operation(summary = "Get today's queue for the professional")
    public ResponseEntity<List<AppointmentResponseDTO>> getQueue(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("");
        return ResponseEntity.ok(getQueue.execute(userDetails.getUserId(), role));
    }

    @PutMapping("/{appointmentId}/call")
    @PreAuthorize("@appointmentPolicy.canCallPatient(authentication)")
    @Operation(summary = "Call next patient (move to IN_PROGRESS)")
    public ResponseEntity<AppointmentResponseDTO> callNextPatient(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(callNextPatient.execute(appointmentId, userDetails.getUserId()));
    }

    @PutMapping("/{appointmentId}/modality")
    @PreAuthorize("@appointmentPolicy.canSetModality(authentication)")
    @Operation(summary = "Set appointment modality (IN_PERSON / ONLINE)")
    public ResponseEntity<AppointmentResponseDTO> setModalityEndpoint(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SetModalityDTO dto) {
        return ResponseEntity.ok(setModality.execute(appointmentId, userDetails.getUserId(), dto));
    }

    @PostMapping("/{appointmentId}/meet-link")
    @PreAuthorize("@appointmentPolicy.canGenerateMeetLink(authentication)")
    @Operation(summary = "Generate Google Meet link for an ONLINE appointment")
    public ResponseEntity<AppointmentResponseDTO> generateMeetLink(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(generateMeetLink.execute(appointmentId, userDetails.getUserId()));
    }
}
