package com.consultafacil.application.service.patient;

import com.consultafacil.api.dto.appointment.MedicalHistoryResponseDTO;
import com.consultafacil.domain.entity.MedicalHistory;
import org.springframework.stereotype.Component;

@Component
public class MedicalHistoryMapper {

    public MedicalHistoryResponseDTO toResponseDTO(MedicalHistory a) {
        return MedicalHistoryResponseDTO.builder()
                .id(a.getId())
                .appointmentId(a.getAppointment().getId())
                .chiefComplaint(a.getChiefComplaint())
                .currentMedications(a.getCurrentMedications())
                .allergies(a.getAllergies())
                .medicalHistory(a.getMedicalHistory())
                .familyHistory(a.getFamilyHistory())
                .observations(a.getObservations())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
