package com.consultafacil.application.service;

import com.consultafacil.api.dto.patient.EmergencyContactDTO;
import com.consultafacil.api.dto.patient.PatientDocumentResponseDTO;
import com.consultafacil.api.dto.patient.PatientVaccineDTO;
import com.consultafacil.application.port.in.PatientHealthUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.EmergencyContact;
import com.consultafacil.domain.entity.PatientDocument;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.PatientVaccine;
import com.consultafacil.domain.enums.DocumentType;
import com.consultafacil.domain.port.out.EmergencyContactRepositoryPort;
import com.consultafacil.domain.port.out.PatientDocumentRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.PatientVaccineRepositoryPort;
import com.consultafacil.domain.port.out.StoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientHealthService implements PatientHealthUseCase {

    private final PatientProfileRepositoryPort patientProfileRepository;
    private final EmergencyContactRepositoryPort emergencyContactRepository;
    private final PatientVaccineRepositoryPort vaccineRepository;
    private final PatientDocumentRepositoryPort documentRepository;
    private final StoragePort storagePort;

    // ── Emergency Contacts ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyContactDTO> listEmergencyContacts(String userId) {
        PatientProfile profile = findByUserId(userId);
        return emergencyContactRepository.findByPatientProfileId(profile.getId())
                .stream().map(this::toContactDTO).toList();
    }

    @Override
    @Transactional
    public EmergencyContactDTO addEmergencyContact(String userId, EmergencyContactDTO dto) {
        PatientProfile profile = findByUserId(userId);
        EmergencyContact contact = EmergencyContact.builder()
                .name(dto.name())
                .phone(dto.phone())
                .email(dto.email())
                .relationship(dto.relationship())
                .patientProfile(profile)
                .build();
        return toContactDTO(emergencyContactRepository.save(contact));
    }

    @Override
    @Transactional
    public EmergencyContactDTO updateEmergencyContact(String userId, String contactId, EmergencyContactDTO dto) {
        PatientProfile profile = findByUserId(userId);
        EmergencyContact contact = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyContact", contactId));
        assertOwnership(contact.getPatientProfile().getId(), profile.getId());
        contact.setName(dto.name());
        contact.setPhone(dto.phone());
        contact.setEmail(dto.email());
        contact.setRelationship(dto.relationship());
        return toContactDTO(emergencyContactRepository.save(contact));
    }

    @Override
    @Transactional
    public void deleteEmergencyContact(String userId, String contactId) {
        PatientProfile profile = findByUserId(userId);
        EmergencyContact contact = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyContact", contactId));
        assertOwnership(contact.getPatientProfile().getId(), profile.getId());
        emergencyContactRepository.delete(contact);
    }

    // ── Vaccines ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<PatientVaccineDTO> listVaccines(String userId) {
        PatientProfile profile = findByUserId(userId);
        return vaccineRepository.findByPatientProfileId(profile.getId())
                .stream().map(this::toVaccineDTO).toList();
    }

    @Override
    @Transactional
    public PatientVaccineDTO addVaccine(String userId, PatientVaccineDTO dto) {
        PatientProfile profile = findByUserId(userId);
        PatientVaccine vaccine = PatientVaccine.builder()
                .vaccineName(dto.vaccineName())
                .doseNumber(dto.doseNumber())
                .administeredAt(dto.administeredAt())
                .notes(dto.notes())
                .patientProfile(profile)
                .build();
        return toVaccineDTO(vaccineRepository.save(vaccine));
    }

    @Override
    @Transactional
    public void deleteVaccine(String userId, String vaccineId) {
        PatientProfile profile = findByUserId(userId);
        PatientVaccine vaccine = vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new ResourceNotFoundException("PatientVaccine", vaccineId));
        assertOwnership(vaccine.getPatientProfile().getId(), profile.getId());
        vaccineRepository.delete(vaccine);
    }

    // ── Documents ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<PatientDocumentResponseDTO> listDocuments(String userId) {
        PatientProfile profile = findByUserId(userId);
        return documentRepository.findByPatientProfileId(profile.getId())
                .stream().map(this::toDocumentDTO).toList();
    }

    @Override
    @Transactional
    public PatientDocumentResponseDTO uploadDocument(String userId, MultipartFile file,
                                                     DocumentType documentType, String documentLabel) {
        PatientProfile profile = findByUserId(userId);
        String fileUrl;
        try {
            fileUrl = storagePort.upload(file.getBytes(), file.getOriginalFilename(),
                    file.getContentType(), "patient-documents");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload document", e);
        }
        PatientDocument doc = PatientDocument.builder()
                .patientProfile(profile)
                .documentType(documentType)
                .documentLabel(documentLabel)
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .build();
        return toDocumentDTO(documentRepository.save(doc));
    }

    @Override
    @Transactional
    public void deleteDocument(String userId, String documentId) {
        PatientProfile profile = findByUserId(userId);
        PatientDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("PatientDocument", documentId));
        assertOwnership(doc.getPatientProfile().getId(), profile.getId());
        String key = doc.getFileUrl().split("amazonaws.com/", 2)[1];
        storagePort.delete(key);
        documentRepository.delete(doc);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private PatientProfile findByUserId(String userId) {
        return patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile", userId));
    }

    private void assertOwnership(String ownerProfileId, String requestorProfileId) {
        if (!ownerProfileId.equals(requestorProfileId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
        }
    }

    private EmergencyContactDTO toContactDTO(EmergencyContact c) {
        return new EmergencyContactDTO(c.getId(), c.getName(), c.getPhone(), c.getEmail(), c.getRelationship());
    }

    private PatientVaccineDTO toVaccineDTO(PatientVaccine v) {
        return new PatientVaccineDTO(v.getId(), v.getVaccineName(), v.getDoseNumber(), v.getAdministeredAt(), v.getNotes());
    }

    private PatientDocumentResponseDTO toDocumentDTO(PatientDocument d) {
        return new PatientDocumentResponseDTO(
                d.getId(),
                d.getDocumentType() != null ? d.getDocumentType().name() : null,
                d.getDocumentLabel(),
                d.getFileUrl(),
                d.getFileName(),
                d.getUploadedAt() != null ? d.getUploadedAt().toString() : null
        );
    }
}
