package com.consultafacil.application.service.patient;

import com.consultafacil.domain.entity.MedicalRecord;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MedicalRecordMapper {

    public Map<String, Object> toResponseMap(MedicalRecord record) {
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
