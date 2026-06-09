package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.MedicalHistoryResponseDTO;
import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.api.dto.appointment.CancelAppointmentDTO;
import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import com.consultafacil.api.dto.appointment.ClinicalNoteResponseDTO;
import com.consultafacil.api.dto.appointment.QrCheckInTokenDTO;
import com.consultafacil.api.dto.appointment.RateAppointmentDTO;
import com.consultafacil.api.dto.appointment.RescheduleAppointmentDTO;
import com.consultafacil.api.dto.appointment.SetModalityDTO;
import com.consultafacil.api.dto.appointment.SaveMedicalHistoryDTO;
import com.consultafacil.api.dto.appointment.SaveClinicalNoteDTO;
import com.consultafacil.application.port.in.AppointmentQueryUseCase;
import com.consultafacil.application.port.in.CallNextPatientUseCase;
import com.consultafacil.application.port.in.CancelAppointmentUseCase;
import com.consultafacil.application.port.in.CheckInByQrUseCase;
import com.consultafacil.application.port.in.CompleteAppointmentUseCase;
import com.consultafacil.application.port.in.ConfirmAppointmentUseCase;
import com.consultafacil.application.port.in.DeleteAppointmentUseCase;
import com.consultafacil.application.port.in.GenerateCheckInTokenUseCase;
import com.consultafacil.application.port.in.GenerateMeetLinkUseCase;
import com.consultafacil.application.port.in.GetQueueUseCase;
import com.consultafacil.application.port.in.RateAppointmentUseCase;
import com.consultafacil.application.port.in.RescheduleAppointmentUseCase;
import com.consultafacil.application.port.in.ScheduleAppointmentUseCase;
import com.consultafacil.application.port.in.SetAppointmentModalityUseCase;
import com.consultafacil.application.port.in.command.CancelAppointmentCommand;
import com.consultafacil.application.port.in.command.RateAppointmentCommand;
import com.consultafacil.application.port.in.command.RescheduleAppointmentCommand;
import com.consultafacil.application.port.in.command.ScheduleAppointmentCommand;
import com.consultafacil.application.port.in.ClinicalNoteUseCase;
import com.consultafacil.application.port.in.MedicalHistoryUseCase;
import com.consultafacil.domain.enums.AppointmentSource;
import com.consultafacil.core.security.CustomUserDetails;
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

    private final ScheduleAppointmentUseCase scheduleAppointment;
    private final ConfirmAppointmentUseCase confirmAppointment;
    private final CancelAppointmentUseCase cancelAppointment;
    private final CompleteAppointmentUseCase completeAppointment;
    private final RateAppointmentUseCase rateAppointment;
    private final DeleteAppointmentUseCase deleteAppointment;
    private final AppointmentQueryUseCase appointmentQuery;
    private final RescheduleAppointmentUseCase rescheduleAppointment;
    private final GenerateCheckInTokenUseCase generateCheckInToken;
    private final CheckInByQrUseCase checkInByQr;
    private final GetQueueUseCase getQueue;
    private final CallNextPatientUseCase callNextPatient;
    private final SetAppointmentModalityUseCase setModality;
    private final GenerateMeetLinkUseCase generateMeetLink;
    private final MedicalHistoryUseCase medicalHistoryUseCase;
    private final ClinicalNoteUseCase clinicalNoteUseCase;

    @PostMapping
    @PreAuthorize("@policy.canScheduleAppointment(authentication)")
    @Operation(summary = "Schedule appointment")
    public ResponseEntity<AppointmentResponseDTO> scheduleAppointment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateAppointmentDTO dto) {
        ScheduleAppointmentCommand command = new ScheduleAppointmentCommand(
                userDetails.getUserId(),
                dto.getProfessionalId(),
                dto.getScheduledAt(),
                dto.getReason(),
                dto.getNotes(),
                dto.getModality(),
                dto.getServiceId(),
                dto.getChosenPaymentMethod());
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleAppointment.execute(command));
    }

    @GetMapping("/{appointmentId}")
    @PreAuthorize("@policy.canViewAnamnesis(authentication)")
    @Operation(summary = "Get appointment by ID")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(appointmentQuery.getById(appointmentId, userDetails.getUserId()));
    }

    @GetMapping("/patient/{userId}")
    @PreAuthorize("@policy.canViewPatientAppointments(authentication)")
    @Operation(summary = "List patient appointments")
    public ResponseEntity<Page<AppointmentResponseDTO>> getPatientAppointments(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(
                appointmentQuery.getPatientAppointments(userId, userDetails.getUserId(), isAdmin, pageable));
    }

    @GetMapping("/professional/{professionalId}")
    @PreAuthorize("@policy.canViewProfessionalAppointments(authentication)")
    @Operation(summary = "List professional appointments")
    public ResponseEntity<Page<AppointmentResponseDTO>> getProfessionalAppointments(
            @PathVariable String professionalId,
            @RequestParam(required = false) AppointmentSource source,
            Pageable pageable) {
        if (source != null) {
            return ResponseEntity.ok(appointmentQuery.getProfessionalAppointmentsBySource(professionalId, source, pageable));
        }
        return ResponseEntity.ok(appointmentQuery.getProfessionalAppointments(professionalId, pageable));
    }

    @PutMapping("/{appointmentId}/confirm")
    @PreAuthorize("@policy.canConfirmAppointment(authentication)")
    @Operation(summary = "Confirm appointment")
    public ResponseEntity<AppointmentResponseDTO> confirmAppointment(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(confirmAppointment.confirm(appointmentId, userDetails.getUserId()));
    }

    @PutMapping("/{appointmentId}/reschedule")
    @PreAuthorize("@policy.canRescheduleAppointment(authentication)")
    @Operation(summary = "Reschedule appointment")
    public ResponseEntity<AppointmentResponseDTO> rescheduleAppointment(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RescheduleAppointmentDTO dto) {
        RescheduleAppointmentCommand command = new RescheduleAppointmentCommand(
                appointmentId, userDetails.getUserId(), dto.getScheduledAt(), dto.getReason());
        return ResponseEntity.ok(rescheduleAppointment.execute(command));
    }

    @PutMapping("/{appointmentId}/cancel")
    @PreAuthorize("@policy.canCancelAppointment(authentication)")
    @Operation(summary = "Cancel appointment")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CancelAppointmentDTO dto) {
        CancelAppointmentCommand command = new CancelAppointmentCommand(
                appointmentId, userDetails.getUserId(), dto.getCancellationReason());
        return ResponseEntity.ok(cancelAppointment.execute(command));
    }

    @PutMapping("/{appointmentId}/complete")
    @PreAuthorize("@policy.canCompleteAppointment(authentication)")
    @Operation(summary = "Complete appointment")
    public ResponseEntity<AppointmentResponseDTO> completeAppointment(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(completeAppointment.complete(appointmentId, userDetails.getUserId()));
    }

    @PostMapping("/{appointmentId}/rate")
    @PreAuthorize("@policy.canRateAppointment(authentication)")
    @Operation(summary = "Rate a completed appointment")
    public ResponseEntity<AppointmentResponseDTO> rateAppointment(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RateAppointmentDTO dto) {
        RateAppointmentCommand command = new RateAppointmentCommand(
                appointmentId, userDetails.getUserId(), dto.getStars(), dto.getComment());
        return ResponseEntity.ok(rateAppointment.execute(command));
    }

    @GetMapping("/{appointmentId}/checkin-token")
    @PreAuthorize("@policy.canGenerateCheckInToken(authentication)")
    @Operation(summary = "Generate QR check-in token for patient")
    public ResponseEntity<QrCheckInTokenDTO> generateCheckInToken(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(generateCheckInToken.execute(appointmentId, userDetails.getUserId()));
    }

    @PostMapping("/checkin")
    @PreAuthorize("@policy.canCheckIn(authentication)")
    @Operation(summary = "Check in patient via QR token")
    public ResponseEntity<AppointmentResponseDTO> checkInByQr(@RequestParam String token) {
        return ResponseEntity.ok(checkInByQr.execute(token));
    }

    @GetMapping("/queue")
    @PreAuthorize("@policy.canViewQueue(authentication)")
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
    @PreAuthorize("@policy.canCallPatient(authentication)")
    @Operation(summary = "Call next patient (move to IN_PROGRESS)")
    public ResponseEntity<AppointmentResponseDTO> callNextPatient(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(callNextPatient.execute(appointmentId, userDetails.getUserId()));
    }

    @PutMapping("/{appointmentId}/modality")
    @PreAuthorize("@policy.canSetModality(authentication)")
    @Operation(summary = "Set appointment modality (IN_PERSON / ONLINE)")
    public ResponseEntity<AppointmentResponseDTO> setModalityEndpoint(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SetModalityDTO dto) {
        return ResponseEntity.ok(setModality.execute(appointmentId, userDetails.getUserId(), dto));
    }

    @PostMapping("/{appointmentId}/meet-link")
    @PreAuthorize("@policy.canGenerateMeetLink(authentication)")
    @Operation(summary = "Generate Google Meet link for an ONLINE appointment")
    public ResponseEntity<AppointmentResponseDTO> generateMeetLink(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(generateMeetLink.execute(appointmentId, userDetails.getUserId()));
    }

    @DeleteMapping("/{appointmentId}")
    @PreAuthorize("@policy.canDeleteAppointment(authentication)")
    @Operation(summary = "Delete appointment")
    public ResponseEntity<Void> deleteAppointment(@PathVariable String appointmentId) {
        deleteAppointment.delete(appointmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{appointmentId}/anamnesis")
    @PreAuthorize("@policy.canViewAnamnesis(authentication)")
    @Operation(summary = "Get anamnesis for an appointment")
    public ResponseEntity<MedicalHistoryResponseDTO> getAnamnesis(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return medicalHistoryUseCase.getByAppointmentId(appointmentId, userDetails.getUserId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/{appointmentId}/anamnesis")
    @PreAuthorize("@policy.canSaveAnamnesis(authentication)")
    @Operation(summary = "Save anamnesis for an appointment")
    public ResponseEntity<MedicalHistoryResponseDTO> saveAnamnesis(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SaveMedicalHistoryDTO dto) {
        return ResponseEntity.ok(medicalHistoryUseCase.save(appointmentId, userDetails.getUserId(), dto));
    }

    @GetMapping("/{appointmentId}/clinicalNote")
    @PreAuthorize("@policy.canViewClinicalNote(authentication)")
    @Operation(summary = "Get clinical note for an appointment")
    public ResponseEntity<ClinicalNoteResponseDTO> getClinicalNote(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return clinicalNoteUseCase.getByAppointmentId(appointmentId, userDetails.getUserId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/{appointmentId}/clinicalNote")
    @PreAuthorize("@policy.canSaveClinicalNote(authentication)")
    @Operation(summary = "Save clinicalNote for an appointment")
    public ResponseEntity<ClinicalNoteResponseDTO> saveClinicalNote(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SaveClinicalNoteDTO dto) {
        return ResponseEntity.ok(clinicalNoteUseCase.save(appointmentId, userDetails.getUserId(), dto));
    }
}
