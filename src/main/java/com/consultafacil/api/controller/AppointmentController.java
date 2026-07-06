package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.api.dto.appointment.CancelAppointmentDTO;
import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import com.consultafacil.api.dto.appointment.RateAppointmentDTO;
import com.consultafacil.api.dto.appointment.RescheduleAppointmentDTO;
import com.consultafacil.application.port.in.CancelAppointmentUseCase;
import com.consultafacil.application.port.in.CompleteAppointmentUseCase;
import com.consultafacil.application.port.in.ConfirmAppointmentUseCase;
import com.consultafacil.application.port.in.DeleteAppointmentUseCase;
import com.consultafacil.application.port.in.GetAllAppointmentsUseCase;
import com.consultafacil.application.port.in.GetAppointmentByIdUseCase;
import com.consultafacil.application.port.in.GetPatientAppointmentsUseCase;
import com.consultafacil.application.port.in.GetProfessionalAppointmentsBySourceUseCase;
import com.consultafacil.application.port.in.GetProfessionalAppointmentsUseCase;
import com.consultafacil.application.port.in.RateAppointmentUseCase;
import com.consultafacil.application.port.in.RescheduleAppointmentUseCase;
import com.consultafacil.application.port.in.ScheduleAppointmentUseCase;
import com.consultafacil.application.port.in.command.CancelAppointmentCommand;
import com.consultafacil.application.port.in.command.RateAppointmentCommand;
import com.consultafacil.application.port.in.command.RescheduleAppointmentCommand;
import com.consultafacil.application.port.in.command.ScheduleAppointmentCommand;
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
    private final GetAppointmentByIdUseCase getAppointmentById;
    private final GetPatientAppointmentsUseCase getPatientAppointments;
    private final GetAllAppointmentsUseCase getAllAppointments;
    private final GetProfessionalAppointmentsUseCase getProfessionalAppointments;
    private final GetProfessionalAppointmentsBySourceUseCase getProfessionalAppointmentsBySource;
    private final RescheduleAppointmentUseCase rescheduleAppointment;

    @PostMapping
    @PreAuthorize("@appointmentPolicy.canScheduleAppointment(authentication)")
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
    @PreAuthorize("@appointmentPolicy.canViewAnamnesis(authentication)")
    @Operation(summary = "Get appointment by ID")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(getAppointmentById.execute(appointmentId, userDetails.getUserId()));
    }

    @GetMapping("/patient/{userId}")
    @PreAuthorize("@appointmentPolicy.canViewPatientAppointments(authentication)")
    @Operation(summary = "List patient appointments")
    public ResponseEntity<Page<AppointmentResponseDTO>> getPatientAppointments(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(
                getPatientAppointments.execute(userId, userDetails.getUserId(), isAdmin, pageable));
    }

    @GetMapping
    @PreAuthorize("@appointmentPolicy.canDeleteAppointment(authentication)")
    @Operation(summary = "List all appointments (admin)")
    public ResponseEntity<Page<AppointmentResponseDTO>> getAllAppointments(Pageable pageable) {
        return ResponseEntity.ok(getAllAppointments.execute(pageable));
    }

    @GetMapping("/professional/{professionalId}")
    @PreAuthorize("@appointmentPolicy.canViewProfessionalAppointments(authentication)")
    @Operation(summary = "List professional appointments")
    public ResponseEntity<Page<AppointmentResponseDTO>> getProfessionalAppointments(
            @PathVariable String professionalId,
            @RequestParam(required = false) AppointmentSource source,
            Pageable pageable) {
        if (source != null) {
            return ResponseEntity.ok(getProfessionalAppointmentsBySource.execute(professionalId, source, pageable));
        }
        return ResponseEntity.ok(getProfessionalAppointments.execute(professionalId, pageable));
    }

    @PutMapping("/{appointmentId}/confirm")
    @PreAuthorize("@appointmentPolicy.canConfirmAppointment(authentication)")
    @Operation(summary = "Confirm appointment")
    public ResponseEntity<AppointmentResponseDTO> confirmAppointment(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(confirmAppointment.confirm(appointmentId, userDetails.getUserId()));
    }

    @PutMapping("/{appointmentId}/reschedule")
    @PreAuthorize("@appointmentPolicy.canRescheduleAppointment(authentication)")
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
    @PreAuthorize("@appointmentPolicy.canCancelAppointment(authentication)")
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
    @PreAuthorize("@appointmentPolicy.canCompleteAppointment(authentication)")
    @Operation(summary = "Complete appointment")
    public ResponseEntity<AppointmentResponseDTO> completeAppointment(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(completeAppointment.complete(appointmentId, userDetails.getUserId()));
    }

    @PostMapping("/{appointmentId}/rate")
    @PreAuthorize("@appointmentPolicy.canRateAppointment(authentication)")
    @Operation(summary = "Rate a completed appointment")
    public ResponseEntity<AppointmentResponseDTO> rateAppointment(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RateAppointmentDTO dto) {
        RateAppointmentCommand command = new RateAppointmentCommand(
                appointmentId, userDetails.getUserId(), dto.getStars(), dto.getComment());
        return ResponseEntity.ok(rateAppointment.execute(command));
    }

    @DeleteMapping("/{appointmentId}")
    @PreAuthorize("@appointmentPolicy.canDeleteAppointment(authentication)")
    @Operation(summary = "Delete appointment")
    public ResponseEntity<Void> deleteAppointment(@PathVariable String appointmentId) {
        deleteAppointment.delete(appointmentId);
        return ResponseEntity.noContent().build();
    }
}
