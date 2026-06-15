package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.PatientSummaryDTO;
import com.consultafacil.api.dto.patient.EmergencyContactDTO;
import com.consultafacil.api.dto.patient.PatientDocumentResponseDTO;
import com.consultafacil.api.dto.patient.PatientVaccineDTO;
import com.consultafacil.application.port.in.AppointmentQueryUseCase;
import com.consultafacil.application.port.in.PatientHealthUseCase;
import com.consultafacil.application.port.in.PatientProfileUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.SecurityUtils;
import com.consultafacil.domain.enums.DocumentType;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Patients", description = "Patient profile management endpoints")
public class PatientProfileController {

    private final PatientProfileUseCase patientProfileUseCase;
    private final PatientHealthUseCase patientHealthUseCase;
    private final AppointmentQueryUseCase appointmentQueryUseCase;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;

    @GetMapping("/professional/{userId}")
    @PreAuthorize("@policy.canViewPatientProfile(authentication)")
    @Operation(summary = "List professional's patients")
    public ResponseEntity<Page<PatientSummaryDTO>> getProfessionalPatients(
            @PathVariable String userId,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String professionalProfileId = professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional profile not found for user: " + userId))
                .getId();
        return ResponseEntity.ok(
                appointmentQueryUseCase.getProfessionalPatients(professionalProfileId, search, sort, page, size));
    }

    @GetMapping("/me")
    @PreAuthorize("@policy.canManagePatientProfile(authentication)")
    @Operation(summary = "Get my patient profile")
    public ResponseEntity<?> getMyProfile() {
        return ResponseEntity.ok(patientProfileUseCase.getPatientProfile(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get patient profile by user ID")
    public ResponseEntity<?> getPatientProfile(@PathVariable String userId) {
        return ResponseEntity.ok(patientProfileUseCase.getPatientProfile(userId));
    }

    @PutMapping("/me")
    @PreAuthorize("@policy.canManagePatientProfile(authentication)")
    @Operation(summary = "Update my patient profile")
    public ResponseEntity<?> updateMyProfile(@RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(
                patientProfileUseCase.updatePatientProfile(SecurityUtils.getCurrentUserId(), updates));
    }

    @GetMapping("/{userId}/medical-records")
    @Operation(summary = "Get patient medical records")
    public ResponseEntity<?> getPatientMedicalRecords(@PathVariable String userId) {
        return ResponseEntity.ok(patientProfileUseCase.getPatientMedicalRecords(userId));
    }

    @PutMapping("/{userId}/medical-records")
    @PreAuthorize("@policy.canManagePatientProfile(authentication)")
    @Operation(summary = "Update patient medical records")
    public ResponseEntity<?> updatePatientMedicalRecords(
            @PathVariable String userId, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(patientProfileUseCase.updatePatientMedicalRecords(userId, updates));
    }

    @GetMapping
    @PreAuthorize("@policy.canAdminListPatients(authentication)")
    @Operation(summary = "List all patients (admin)")
    public ResponseEntity<Page<Map<String, Object>>> getAllPatients(Pageable pageable) {
        return ResponseEntity.ok(patientProfileUseCase.getAllPatients(pageable));
    }

    // ── Emergency Contacts ────────────────────────────────────────────────

    @GetMapping("/me/emergency-contacts")
    @PreAuthorize("@policy.canManageOwnEmergencyContacts(authentication)")
    @Operation(summary = "List my emergency contacts")
    public ResponseEntity<List<EmergencyContactDTO>> listEmergencyContacts() {
        return ResponseEntity.ok(patientHealthUseCase.listEmergencyContacts(SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/me/emergency-contacts")
    @PreAuthorize("@policy.canManageOwnEmergencyContacts(authentication)")
    @Operation(summary = "Add an emergency contact")
    public ResponseEntity<EmergencyContactDTO> addEmergencyContact(
            @Valid @RequestBody EmergencyContactDTO dto) {
        return ResponseEntity.ok(patientHealthUseCase.addEmergencyContact(SecurityUtils.getCurrentUserId(), dto));
    }

    @PutMapping("/me/emergency-contacts/{contactId}")
    @PreAuthorize("@policy.canManageOwnEmergencyContacts(authentication)")
    @Operation(summary = "Update an emergency contact")
    public ResponseEntity<EmergencyContactDTO> updateEmergencyContact(
            @PathVariable String contactId, @Valid @RequestBody EmergencyContactDTO dto) {
        return ResponseEntity.ok(
                patientHealthUseCase.updateEmergencyContact(SecurityUtils.getCurrentUserId(), contactId, dto));
    }

    @DeleteMapping("/me/emergency-contacts/{contactId}")
    @PreAuthorize("@policy.canManageOwnEmergencyContacts(authentication)")
    @Operation(summary = "Delete an emergency contact")
    public ResponseEntity<Void> deleteEmergencyContact(@PathVariable String contactId) {
        patientHealthUseCase.deleteEmergencyContact(SecurityUtils.getCurrentUserId(), contactId);
        return ResponseEntity.noContent().build();
    }

    // ── Vaccines ──────────────────────────────────────────────────────────

    @GetMapping("/me/vaccines")
    @PreAuthorize("@policy.canManageOwnVaccines(authentication)")
    @Operation(summary = "List my vaccines")
    public ResponseEntity<List<PatientVaccineDTO>> listVaccines() {
        return ResponseEntity.ok(patientHealthUseCase.listVaccines(SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/me/vaccines")
    @PreAuthorize("@policy.canManageOwnVaccines(authentication)")
    @Operation(summary = "Add a vaccine")
    public ResponseEntity<PatientVaccineDTO> addVaccine(@Valid @RequestBody PatientVaccineDTO dto) {
        return ResponseEntity.ok(patientHealthUseCase.addVaccine(SecurityUtils.getCurrentUserId(), dto));
    }

    @DeleteMapping("/me/vaccines/{vaccineId}")
    @PreAuthorize("@policy.canManageOwnVaccines(authentication)")
    @Operation(summary = "Delete a vaccine")
    public ResponseEntity<Void> deleteVaccine(@PathVariable String vaccineId) {
        patientHealthUseCase.deleteVaccine(SecurityUtils.getCurrentUserId(), vaccineId);
        return ResponseEntity.noContent().build();
    }

    // ── Documents ─────────────────────────────────────────────────────────

    @GetMapping("/me/documents")
    @PreAuthorize("@policy.canManageOwnDocuments(authentication)")
    @Operation(summary = "List my documents")
    public ResponseEntity<List<PatientDocumentResponseDTO>> listDocuments() {
        return ResponseEntity.ok(patientHealthUseCase.listDocuments(SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/me/documents")
    @PreAuthorize("@policy.canManageOwnDocuments(authentication)")
    @Operation(summary = "Upload a document")
    public ResponseEntity<PatientDocumentResponseDTO> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam(value = "documentLabel", required = false) String documentLabel) {
        return ResponseEntity.ok(
                patientHealthUseCase.uploadDocument(SecurityUtils.getCurrentUserId(), file, documentType, documentLabel));
    }

    @DeleteMapping("/me/documents/{documentId}")
    @PreAuthorize("@policy.canManageOwnDocuments(authentication)")
    @Operation(summary = "Delete a document")
    public ResponseEntity<Void> deleteDocument(@PathVariable String documentId) {
        patientHealthUseCase.deleteDocument(SecurityUtils.getCurrentUserId(), documentId);
        return ResponseEntity.noContent().build();
    }
}
