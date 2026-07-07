package com.consultafacil.application.service.patient;

import com.consultafacil.application.port.in.patient.UpdatePatientMedicalRecordsUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.MedicalRecord;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.enums.BloodType;
import com.consultafacil.domain.port.out.patient.MedicalRecordRepositoryPort;
import com.consultafacil.domain.port.out.patient.PatientProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UpdatePatientMedicalRecordsService implements UpdatePatientMedicalRecordsUseCase {

    private final PatientProfileRepositoryPort patientProfileRepository;
    private final MedicalRecordRepositoryPort medicalRecordRepository;
    private final MedicalRecordMapper mapper;

    @Override
    @Transactional
    public Map<String, Object> execute(String userId, Map<String, Object> updates) {
        PatientProfile patientProfile = patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + userId));

        MedicalRecord medicalRecord = patientProfile.getMedicalRecord();
        if (medicalRecord == null) {
            medicalRecord = MedicalRecord.builder().patientProfile(patientProfile).build();
        }

        applyUpdates(medicalRecord, updates);

        return mapper.toResponseMap(medicalRecordRepository.save(medicalRecord));
    }

    private void applyUpdates(MedicalRecord medicalRecord, Map<String, Object> updates) {
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
            medicalRecord.setHeight(h != null ? new BigDecimal(h.toString()) : null);
        }
        if (updates.containsKey("weight")) {
            Object w = updates.get("weight");
            medicalRecord.setWeight(w != null ? new BigDecimal(w.toString()) : null);
        }
    }
}
