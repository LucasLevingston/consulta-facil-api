package com.example.consulta.api.controller;

import com.example.consulta.api.dto.appointment.MedicalHistoryResponseDTO;
import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;
import com.example.consulta.api.dto.appointment.CancelAppointmentDTO;
import com.example.consulta.api.dto.appointment.CreateAppointmentDTO;
import com.example.consulta.api.dto.appointment.ClinicalNoteResponseDTO;
import com.example.consulta.api.dto.appointment.QrCheckInTokenDTO;
import com.example.consulta.api.dto.appointment.RateAppointmentDTO;
import com.example.consulta.api.dto.appointment.RescheduleAppointmentDTO;
import com.example.consulta.api.dto.appointment.SetModalityDTO;
import com.example.consulta.api.dto.appointment.SaveMedicalHistoryDTO;
import com.example.consulta.api.dto.appointment.SaveClinicalNoteDTO;
import com.example.consulta.application.service.MedicalHistoryService;
import com.example.consulta.application.service.AppointmentService;
import com.example.consulta.application.service.CallNextPatientService;
import com.example.consulta.application.service.CheckInByQrService;
import com.example.consulta.application.service.GenerateCheckInTokenService;
import com.example.consulta.application.service.GenerateMeetLinkService;
import com.example.consulta.application.service.GetQueueService;
import com.example.consulta.application.service.ClinicalNoteService;
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
    private final MedicalHistoryService medicalHistoryService;
    private final ClinicalNoteService clinicalNoteService;
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
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get appointment by ID")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(appointmentId, userDetails.getUserId()));
    }

    @GetMapping("/patient/{userId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    @Operation(summary = "List patient appointments")
    public ResponseEntity<Page<AppointmentResponseDTO>> getPatientAppointments(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(appointmentService.getPatientAppointments(userId, userDetails.getUserId(), isAdmin, pageable));
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
    public ResponseEntity<AppointmentResponseDTO> confirmAppointment(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AppointmentResponseDTO response = appointmentService.confirmAppointment(appointmentId, userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{appointmentId}/reschedule")
    @PreAuthorize("hasAnyRole('PATIENT', 'PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "Reschedule appointment")
    public ResponseEntity<AppointmentResponseDTO> rescheduleAppointment(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RescheduleAppointmentDTO dto) {
        return ResponseEntity.ok(rescheduleAppointmentService.execute(appointmentId, userDetails.getUserId(), dto));
    }

    @PutMapping("/{appointmentId}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel appointment")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CancelAppointmentDTO dto) {
        AppointmentResponseDTO response = appointmentService.cancelAppointment(appointmentId, userDetails.getUserId(), dto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{appointmentId}/complete")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "Complete appointment")
    public ResponseEntity<AppointmentResponseDTO> completeAppointment(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AppointmentResponseDTO response = appointmentService.completeAppointment(appointmentId, userDetails.getUserId());
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
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("");
        return ResponseEntity.ok(getQueueService.execute(userDetails.getUserId(), role));
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
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get anamnesis for an appointment")
    public ResponseEntity<MedicalHistoryResponseDTO> getAnamnesis(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return medicalHistoryService.getByAppointmentId(appointmentId, userDetails.getUserId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/{appointmentId}/anamnesis")
    @PreAuthorize("hasAnyRole('PATIENT', 'PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "Save anamnesis for an appointment")
    public ResponseEntity<MedicalHistoryResponseDTO> saveAnamnesis(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SaveMedicalHistoryDTO dto) {
        return ResponseEntity.ok(medicalHistoryService.save(appointmentId, userDetails.getUserId(), dto));
    }

    @GetMapping("/{appointmentId}/clinicalNote")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get clinical note for an appointment")
    public ResponseEntity<ClinicalNoteResponseDTO> getClinicalNote(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return clinicalNoteService.getByAppointmentId(appointmentId, userDetails.getUserId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/{appointmentId}/clinicalNote")
    @PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
    @Operation(summary = "Save clinicalNote for an appointment")
    public ResponseEntity<ClinicalNoteResponseDTO> saveClinicalNote(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SaveClinicalNoteDTO dto) {
        return ResponseEntity.ok(clinicalNoteService.save(appointmentId, userDetails.getUserId(), dto));
    }
}
