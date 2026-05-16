package com.example.consulta.api.controller;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;
import com.example.consulta.api.dto.appointment.CancelAppointmentDTO;
import com.example.consulta.api.dto.appointment.CreateAppointmentDTO;
import com.example.consulta.application.service.AppointmentService;
import com.example.consulta.core.security.CustomUserDetails;
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
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Appointments", description = "Appointment management endpoints")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
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
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "List patient appointments")
    public ResponseEntity<Page<AppointmentResponseDTO>> getPatientAppointments(
            @PathVariable String userId,
            Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getPatientAppointments(userId, pageable));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List doctor appointments")
    public ResponseEntity<Page<AppointmentResponseDTO>> getDoctorAppointments(
            @PathVariable String doctorId,
            Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(doctorId, pageable));
    }

    @PutMapping("/{appointmentId}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Confirm appointment")
    public ResponseEntity<AppointmentResponseDTO> confirmAppointment(@PathVariable String appointmentId) {
        AppointmentResponseDTO response = appointmentService.confirmAppointment(appointmentId);
        return ResponseEntity.ok(response);
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
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Complete appointment")
    public ResponseEntity<AppointmentResponseDTO> completeAppointment(@PathVariable String appointmentId) {
        AppointmentResponseDTO response = appointmentService.completeAppointment(appointmentId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete appointment")
    public ResponseEntity<Void> deleteAppointment(@PathVariable String appointmentId) {
        appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }
}
