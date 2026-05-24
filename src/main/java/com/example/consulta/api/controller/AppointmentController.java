package com.example.consulta.api.controller;

import com.example.consulta.api.dto.appointment.AnamneseResponseDTO;
import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;
import com.example.consulta.api.dto.appointment.CancelAppointmentDTO;
import com.example.consulta.api.dto.appointment.CreateAppointmentDTO;
import com.example.consulta.api.dto.appointment.ProntuarioResponseDTO;
import com.example.consulta.api.dto.appointment.QrCheckInTokenDTO;
import com.example.consulta.api.dto.appointment.RateAppointmentDTO;
import com.example.consulta.api.dto.appointment.RescheduleAppointmentDTO;
import com.example.consulta.api.dto.appointment.SetModalityDTO;
import com.example.consulta.api.dto.appointment.SaveAnamneseDTO;
import com.example.consulta.api.dto.appointment.SaveProntuarioDTO;
import com.example.consulta.application.service.AnamneseService;
import com.example.consulta.application.service.AppointmentService;
import com.example.consulta.application.service.CallNextPatientService;
import com.example.consulta.application.service.CheckInByQrService;
import com.example.consulta.application.service.GenerateCheckInTokenService;
import com.example.consulta.application.service.GenerateMeetLinkService;
import com.example.consulta.application.service.GetQueueService;
import com.example.consulta.application.service.ProntuarioService;
import com.example.consulta.application.service.RescheduleAppointmentService;
import com.example.consulta.application.service.SetAppointmentModalityService;
import com.example.consulta.core.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Appointments", description = "Appointment management endpoints")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final RescheduleAppointmentService rescheduleAppointmentService;
    private final AnamneseService anamneseService;
    private final ProntuarioService prontuarioService;
    private final GenerateCheckInTokenService generateCheckInTokenService;
    private final CheckInByQrService checkInByQrService;
    private final GetQueueService getQueueService;
    private final CallNextPatientService callNextPatientService;
    private final SetAppointmentModalityService setAppointmentModalityService;
    private final GenerateMeetLinkService generateMeetLinkService;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Schedule appointment", description = "Creates a new appointment for the authenticated patient")
    public ResponseEntity<AppointmentResponseDTO> scheduleAppointment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateAppointmentDTO dto) {
        AppointmentResponseDTO response = appointmentService.scheduleAppointment(userDetails.getUserId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{appointmentId}")
    @Operation(summary = "Get appointment by ID")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(@PathVariable String appointmentId) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(appointmentId));
    }

    @GetMapping("/patient/{userId}")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "List patient appointments")
    public ResponseEntity<Page<AppointmentResponseDTO>> getPatientAppointments(
            @PathVariable String userId,
            Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getPatientAppointments(userId, pageable));
    }

    @GetMapping("/professional/{professionalId}")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "List professional appointments")
    public ResponseEntity<Page<AppointmentResponseDTO>> getProfessionalAppointments(
            @PathVariable String professionalId,
            Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getProfessionalAppointments(professionalId, pageable));
    }

    @PutMapping("/{appointmentId}/confirm")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "Confirm appointment")
    public ResponseEntity<AppointmentResponseDTO> confirmAppointment(@PathVariable String appointmentId) {
        AppointmentResponseDTO response = appointmentService.confirmAppointment(appointmentId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{appointmentId}/reschedule")
    @PreAuthorize("hasAnyRole('PATIENT', 'PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "Reschedule appointment")
    public ResponseEntity<AppointmentResponseDTO> rescheduleAppointment(
            @PathVariable String appointmentId,
            @Valid @RequestBody RescheduleAppointmentDTO dto) {
        return ResponseEntity.ok(rescheduleAppointmentService.execute(appointmentId, dto));
    }

    @PutMapping("/{appointmentId}/cancel")
    @Operation(summary = "Cancel appointment")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(
            @PathVariable String appointmentId,
            @Valid @RequestBody CancelAppointmentDTO dto) {
        AppointmentResponseDTO response = appointmentService.cancelAppointment(appointmentId, dto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{appointmentId}/complete")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "Complete appointment")
    public ResponseEntity<AppointmentResponseDTO> completeAppointment(@PathVariable String appointmentId) {
        AppointmentResponseDTO response = appointmentService.completeAppointment(appointmentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{appointmentId}/rate")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Rate a completed appointment")
    public ResponseEntity<AppointmentResponseDTO> rateAppointment(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RateAppointmentDTO dto) {
        AppointmentResponseDTO response = appointmentService.rateAppointment(
                appointmentId, userDetails.getUserId(), dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{appointmentId}/checkin-token")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Generate QR check-in token for patient")
    public ResponseEntity<QrCheckInTokenDTO> generateCheckInToken(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(generateCheckInTokenService.execute(appointmentId, userDetails.getUserId()));
    }

    @PostMapping("/checkin")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "Check in patient via QR token")
    public ResponseEntity<AppointmentResponseDTO> checkInByQr(@RequestParam String token) {
        return ResponseEntity.ok(checkInByQrService.execute(token));
    }

    @GetMapping("/queue")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Get today's queue for the professional")
    public ResponseEntity<List<AppointmentResponseDTO>> getQueue(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(getQueueService.execute(userDetails.getUserId()));
    }

    @PutMapping("/{appointmentId}/call")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "Call next patient (move to IN_PROGRESS)")
    public ResponseEntity<AppointmentResponseDTO> callNextPatient(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(callNextPatientService.execute(appointmentId, userDetails.getUserId()));
    }

    @PutMapping("/{appointmentId}/modality")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "Set appointment modality (IN_PERSON / ONLINE)")
    public ResponseEntity<AppointmentResponseDTO> setModality(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SetModalityDTO dto) {
        return ResponseEntity.ok(setAppointmentModalityService.execute(appointmentId, userDetails.getUserId(), dto));
    }

    @PostMapping("/{appointmentId}/meet-link")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "Generate Google Meet link for an ONLINE appointment")
    public ResponseEntity<AppointmentResponseDTO> generateMeetLink(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(generateMeetLinkService.execute(appointmentId, userDetails.getUserId()));
    }

    @DeleteMapping("/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete appointment")
    public ResponseEntity<Void> deleteAppointment(@PathVariable String appointmentId) {
        appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{appointmentId}/anamnesis")
    @Operation(summary = "Get anamnesis for an appointment")
    public ResponseEntity<AnamneseResponseDTO> getAnamnesis(@PathVariable String appointmentId) {
        return anamneseService.getByAppointmentId(appointmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/{appointmentId}/anamnesis")
    @PreAuthorize("hasAnyRole('PATIENT', 'PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "Save anamnesis for an appointment")
    public ResponseEntity<AnamneseResponseDTO> saveAnamnesis(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SaveAnamneseDTO dto) {
        return ResponseEntity.ok(anamneseService.save(appointmentId, userDetails.getUserId(), dto));
    }

    @GetMapping("/{appointmentId}/prontuario")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get prontuario for an appointment")
    public ResponseEntity<ProntuarioResponseDTO> getProntuario(@PathVariable String appointmentId) {
        return prontuarioService.getByAppointmentId(appointmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/{appointmentId}/prontuario")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "Save prontuario for an appointment")
    public ResponseEntity<ProntuarioResponseDTO> saveProntuario(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SaveProntuarioDTO dto) {
        return ResponseEntity.ok(prontuarioService.save(appointmentId, userDetails.getUserId(), dto));
    }
}
