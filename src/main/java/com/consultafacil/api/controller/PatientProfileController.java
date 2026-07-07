package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.PatientSummaryDTO;
import com.consultafacil.api.dto.patient.EmergencyContactDTO;
import com.consultafacil.api.dto.patient.PatientDocumentResponseDTO;
import com.consultafacil.api.dto.patient.PatientVaccineDTO;
import com.consultafacil.application.port.in.patient.AddEmergencyContactUseCase;
import com.consultafacil.application.port.in.patient.AddVaccineUseCase;
import com.consultafacil.application.port.in.patient.DeleteDocumentUseCase;
import com.consultafacil.application.port.in.patient.DeleteEmergencyContactUseCase;
import com.consultafacil.application.port.in.patient.DeleteVaccineUseCase;
import com.consultafacil.application.port.in.patient.GetAllPatientsUseCase;
import com.consultafacil.application.port.in.patient.GetPatientMedicalRecordsUseCase;
import com.consultafacil.application.port.in.patient.GetPatientProfileUseCase;
import com.consultafacil.application.port.in.professional.profile.GetProfessionalPatientsUseCase;
import com.consultafacil.application.port.in.patient.ListDocumentsUseCase;
import com.consultafacil.application.port.in.patient.ListEmergencyContactsUseCase;
import com.consultafacil.application.port.in.patient.ListVaccinesUseCase;
import com.consultafacil.application.port.in.patient.UpdateEmergencyContactUseCase;
import com.consultafacil.application.port.in.patient.UpdatePatientMedicalRecordsUseCase;
import com.consultafacil.application.port.in.patient.UpdatePatientProfileUseCase;
import com.consultafacil.application.port.in.patient.UploadDocumentUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.SecurityUtils;
import com.consultafacil.domain.enums.DocumentType;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
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

    private final GetPatientProfileUseCase getPatientProfile;
    private final UpdatePatientProfileUseCase updatePatientProfile;
    private final GetPatientMedicalRecordsUseCase getPatientMedicalRecords;
    private final UpdatePatientMedicalRecordsUseCase updatePatientMedicalRecords;
    private final GetAllPatientsUseCase getAllPatients;
    private final ListEmergencyContactsUseCase listEmergencyContacts;
    private final AddEmergencyContactUseCase addEmergencyContact;
    private final UpdateEmergencyContactUseCase updateEmergencyContact;
    private final DeleteEmergencyContactUseCase deleteEmergencyContact;
    private final ListVaccinesUseCase listVaccines;
    private final AddVaccineUseCase addVaccine;
    private final DeleteVaccineUseCase deleteVaccine;
    private final ListDocumentsUseCase listDocuments;
    private final UploadDocumentUseCase uploadDocument;
    private final DeleteDocumentUseCase deleteDocument;
    private final GetProfessionalPatientsUseCase getProfessionalPatients;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;

    @GetMapping("/professional/{userId}")
    @PreAuthorize("@carePolicy.canViewPatientProfile(authentication)")
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
                getProfessionalPatients.execute(professionalProfileId, search, sort, page, size));
    }

    @GetMapping("/me")
    @PreAuthorize("@carePolicy.canManagePatientProfile(authentication)")
    @Operation(summary = "Get my patient profile")
    public ResponseEntity<?> getMyProfile() {
        return ResponseEntity.ok(getPatientProfile.execute(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get patient profile by user ID")
    public ResponseEntity<?> getPatientProfile(@PathVariable String userId) {
        return ResponseEntity.ok(getPatientProfile.execute(userId));
    }

    @PutMapping("/me")
    @PreAuthorize("@carePolicy.canManagePatientProfile(authentication)")
    @Operation(summary = "Update my patient profile")
    public ResponseEntity<?> updateMyProfile(@RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(
                updatePatientProfile.execute(SecurityUtils.getCurrentUserId(), updates));
    }

    @GetMapping("/{userId}/medical-records")
    @Operation(summary = "Get patient medical records")
    public ResponseEntity<?> getPatientMedicalRecords(@PathVariable String userId) {
        return ResponseEntity.ok(getPatientMedicalRecords.execute(userId));
    }

    @PutMapping("/{userId}/medical-records")
    @PreAuthorize("@carePolicy.canManagePatientProfile(authentication)")
    @Operation(summary = "Update patient medical records")
    public ResponseEntity<?> updatePatientMedicalRecords(
            @PathVariable String userId, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(updatePatientMedicalRecords.execute(userId, updates));
    }

    @GetMapping
    @PreAuthorize("@carePolicy.canAdminListPatients(authentication)")
    @Operation(summary = "List all patients (admin)")
    public ResponseEntity<Page<Map<String, Object>>> getAllPatients(Pageable pageable) {
        return ResponseEntity.ok(getAllPatients.execute(pageable));
    }

    // ── Emergency Contacts ────────────────────────────────────────────────

    @GetMapping("/me/emergency-contacts")
    @PreAuthorize("@carePolicy.canManageOwnEmergencyContacts(authentication)")
    @Operation(summary = "List my emergency contacts")
    public ResponseEntity<List<EmergencyContactDTO>> listEmergencyContacts() {
        return ResponseEntity.ok(listEmergencyContacts.execute(SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/me/emergency-contacts")
    @PreAuthorize("@carePolicy.canManageOwnEmergencyContacts(authentication)")
    @Operation(summary = "Add an emergency contact")
    public ResponseEntity<EmergencyContactDTO> addEmergencyContact(
            @Valid @RequestBody EmergencyContactDTO dto) {
        return ResponseEntity.ok(addEmergencyContact.execute(SecurityUtils.getCurrentUserId(), dto));
    }

    @PutMapping("/me/emergency-contacts/{contactId}")
    @PreAuthorize("@carePolicy.canManageOwnEmergencyContacts(authentication)")
    @Operation(summary = "Update an emergency contact")
    public ResponseEntity<EmergencyContactDTO> updateEmergencyContact(
            @PathVariable String contactId, @Valid @RequestBody EmergencyContactDTO dto) {
        return ResponseEntity.ok(
                updateEmergencyContact.execute(SecurityUtils.getCurrentUserId(), contactId, dto));
    }

    @DeleteMapping("/me/emergency-contacts/{contactId}")
    @PreAuthorize("@carePolicy.canManageOwnEmergencyContacts(authentication)")
    @Operation(summary = "Delete an emergency contact")
    public ResponseEntity<Void> deleteEmergencyContact(@PathVariable String contactId) {
        deleteEmergencyContact.execute(SecurityUtils.getCurrentUserId(), contactId);
        return ResponseEntity.noContent().build();
    }

    // ── Vaccines ──────────────────────────────────────────────────────────

    @GetMapping("/me/vaccines")
    @PreAuthorize("@carePolicy.canManageOwnVaccines(authentication)")
    @Operation(summary = "List my vaccines")
    public ResponseEntity<List<PatientVaccineDTO>> listVaccines() {
        return ResponseEntity.ok(listVaccines.execute(SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/me/vaccines")
    @PreAuthorize("@carePolicy.canManageOwnVaccines(authentication)")
    @Operation(summary = "Add a vaccine")
    public ResponseEntity<PatientVaccineDTO> addVaccine(@Valid @RequestBody PatientVaccineDTO dto) {
        return ResponseEntity.ok(addVaccine.execute(SecurityUtils.getCurrentUserId(), dto));
    }

    @DeleteMapping("/me/vaccines/{vaccineId}")
    @PreAuthorize("@carePolicy.canManageOwnVaccines(authentication)")
    @Operation(summary = "Delete a vaccine")
    public ResponseEntity<Void> deleteVaccine(@PathVariable String vaccineId) {
        deleteVaccine.execute(SecurityUtils.getCurrentUserId(), vaccineId);
        return ResponseEntity.noContent().build();
    }

    // ── Documents ─────────────────────────────────────────────────────────

    @GetMapping("/me/documents")
    @PreAuthorize("@carePolicy.canManageOwnDocuments(authentication)")
    @Operation(summary = "List my documents")
    public ResponseEntity<List<PatientDocumentResponseDTO>> listDocuments() {
        return ResponseEntity.ok(listDocuments.execute(SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/me/documents")
    @PreAuthorize("@carePolicy.canManageOwnDocuments(authentication)")
    @Operation(summary = "Upload a document")
    public ResponseEntity<PatientDocumentResponseDTO> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam(value = "documentLabel", required = false) String documentLabel) {
        return ResponseEntity.ok(
                uploadDocument.execute(SecurityUtils.getCurrentUserId(), file, documentType, documentLabel));
    }

    @DeleteMapping("/me/documents/{documentId}")
    @PreAuthorize("@carePolicy.canManageOwnDocuments(authentication)")
    @Operation(summary = "Delete a document")
    public ResponseEntity<Void> deleteDocument(@PathVariable String documentId) {
        deleteDocument.execute(SecurityUtils.getCurrentUserId(), documentId);
        return ResponseEntity.noContent().build();
    }
}
