package com.consultafacil.application.service;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.MedicalRecord;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.BloodType;
import com.consultafacil.domain.port.out.MedicalRecordRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import com.consultafacil.application.port.in.PatientProfileUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientProfileService implements PatientProfileUseCase {

    private final PatientProfileRepositoryPort patientProfileRepository;
    private final UserRepositoryPort userRepository;
    private final MedicalRecordRepositoryPort medicalRecordRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getPatientProfile(String userId) {
        log.debug("Fetching patient profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        PatientProfile patientProfile = patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + userId));

        return toResponseMap(user, patientProfile);
    }

    @Transactional
    public Map<String, Object> updatePatientProfile(String userId, Map<String, Object> updates) {

        PatientProfile patientProfile = patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + userId));

        if (updates.containsKey("occupation")) {
            patientProfile.setOccupation((String) updates.get("occupation"));
        }

        PatientProfile updated = patientProfileRepository.save(patientProfile);
        User user = updated.getUser();

        return toResponseMap(user, updated);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPatientMedicalRecords(String userId) {
        log.debug("Fetching medical records for user: {}", userId);

        PatientProfile patientProfile = patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + userId));

        MedicalRecord medicalRecord = patientProfile.getMedicalRecord();

        if (medicalRecord == null) {
            throw new ResourceNotFoundException("Medical records not found for this patient");
        }

        return toMedicalRecordMap(medicalRecord);
    }

    @Transactional
    public Map<String, Object> updatePatientMedicalRecords(String userId, Map<String, Object> updates) {

        PatientProfile patientProfile = patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + userId));

        MedicalRecord medicalRecord = patientProfile.getMedicalRecord();

        if (medicalRecord == null) {
            medicalRecord = MedicalRecord.builder()
                    .patientProfile(patientProfile)
                    .build();
        }

        if (updates.containsKey("allergies")) {
            medicalRecord.setAllergies((String) updates.get("allergies"));
        }
        if (updates.containsKey("currentMedication")) {
            medicalRecord.setCurrentMedication((String) updates.get("currentMedication"));
        }
        if (updates.containsKey("familyMedicalHistory")) {
            medicalRecord.setFamilyMedicalHistory((String) updates.get("familyMedicalHistory"));
        }
        if (updates.containsKey("pastMedicalHistory")) {
            medicalRecord.setPastMedicalHistory((String) updates.get("pastMedicalHistory"));
        }
        if (updates.containsKey("privacyConsent")) {
            medicalRecord.setPrivacyConsent((Boolean) updates.get("privacyConsent"));
        }
        if (updates.containsKey("treatmentConsent")) {
            medicalRecord.setTreatmentConsent((Boolean) updates.get("treatmentConsent"));
        }
        if (updates.containsKey("disclosureConsent")) {
            medicalRecord.setDisclosureConsent((Boolean) updates.get("disclosureConsent"));
        }
        if (updates.containsKey("bloodType") && updates.get("bloodType") != null) {
            medicalRecord.setBloodType(BloodType.valueOf((String) updates.get("bloodType")));
        } else if (updates.containsKey("bloodType")) {
            medicalRecord.setBloodType(null);
        }
        if (updates.containsKey("height")) {
            Object h = updates.get("height");
            medicalRecord.setHeight(h != null ? new java.math.BigDecimal(h.toString()) : null);
        }
        if (updates.containsKey("weight")) {
            Object w = updates.get("weight");
            medicalRecord.setWeight(w != null ? new java.math.BigDecimal(w.toString()) : null);
        }

        MedicalRecord updated = medicalRecordRepository.save(medicalRecord);

        return toMedicalRecordMap(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getAllPatients(Pageable pageable) {
        return patientProfileRepository.findAll(pageable)
                .map(pp -> toResponseMap(pp.getUser(), pp));
    }

    private Map<String, Object> toResponseMap(User user, PatientProfile patientProfile) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", patientProfile.getId());
        response.put("userId", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("cpf", user.getCpf());
        response.put("imageUrl", user.getImageUrl());
        response.put("occupation", patientProfile.getOccupation());
        response.put("birthDate", user.getBirthDate());
        response.put("gender", user.getGender());
        response.put("createdAt", user.getCreatedAt());
        response.put("updatedAt", user.getUpdatedAt());
        return response;
    }

    private Map<String, Object> toMedicalRecordMap(MedicalRecord record) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", record.getId());
        response.put("allergies", record.getAllergies());
        response.put("currentMedication", record.getCurrentMedication());
        response.put("familyMedicalHistory", record.getFamilyMedicalHistory());
        response.put("pastMedicalHistory", record.getPastMedicalHistory());
        response.put("privacyConsent", record.getPrivacyConsent());
        response.put("treatmentConsent", record.getTreatmentConsent());
        response.put("disclosureConsent", record.getDisclosureConsent());
        response.put("bloodType", record.getBloodType() != null ? record.getBloodType().name() : null);
        response.put("height", record.getHeight());
        response.put("weight", record.getWeight());
        response.put("createdAt", record.getCreatedAt());
        response.put("updatedAt", record.getUpdatedAt());
        return response;
    }
}
