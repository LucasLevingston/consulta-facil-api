package com.consultafacil.api.dto.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveMedicalHistoryDTO {
    private String chiefComplaint;
    private String currentMedications;
    private String allergies;
    private String medicalHistory;
    private String familyHistory;
    private String observations;
}
