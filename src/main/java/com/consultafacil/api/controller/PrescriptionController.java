package com.consultafacil.api.controller;

import com.consultafacil.api.dto.prescription.IssuePrescriptionRequest;
import com.consultafacil.api.dto.prescription.PrescriptionDTO;
import com.consultafacil.application.port.in.prescription.DownloadPrescriptionUseCase;
import com.consultafacil.application.port.in.prescription.IssuePrescriptionUseCase;
import com.consultafacil.application.port.in.prescription.ListAppointmentPrescriptionsUseCase;
import com.consultafacil.application.port.in.prescription.ListMyPrescriptionsUseCase;
import com.consultafacil.core.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/prescriptions")
@RequiredArgsConstructor
@Tag(name = "Prescriptions", description = "Digital prescription management")
public class PrescriptionController {

    private final IssuePrescriptionUseCase issuePrescription;
    private final DownloadPrescriptionUseCase downloadPrescription;
    private final ListAppointmentPrescriptionsUseCase listAppointmentPrescriptions;
    private final ListMyPrescriptionsUseCase listMyPrescriptions;

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@adminPolicy.canIssuePrescription(authentication)")
    @Operation(summary = "Issue digital prescription (PROFESSIONAL only)")
    public ResponseEntity<PrescriptionDTO> issue(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody IssuePrescriptionRequest request) {
        return ResponseEntity.ok(issuePrescription.execute(userDetails.getUserId(), request));
    }

    @GetMapping("/{id}/download")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@adminPolicy.canViewPrescriptions(authentication)")
    @Operation(summary = "Get prescription download URL")
    public ResponseEntity<Void> download(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String id) {
        String pdfUrl = downloadPrescription.execute(userDetails.getUserId(), id);
        if (pdfUrl == null || pdfUrl.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(302).location(URI.create(pdfUrl)).build();
    }

    @GetMapping("/appointment/{appointmentId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@adminPolicy.canViewPrescriptions(authentication)")
    @Operation(summary = "List prescriptions for an appointment")
    public ResponseEntity<List<PrescriptionDTO>> listByAppointment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String appointmentId) {
        return ResponseEntity.ok(listAppointmentPrescriptions.execute(userDetails.getUserId(), appointmentId));
    }

    @GetMapping("/mine")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("@adminPolicy.canViewPrescriptions(authentication)")
    @Operation(summary = "List own prescriptions (PATIENT)")
    public ResponseEntity<Page<PrescriptionDTO>> listMine(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        return ResponseEntity.ok(listMyPrescriptions.execute(userDetails.getUserId(), pageable));
    }
}
