package com.consultafacil.api.dto.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveClinicalNoteDTO {
    private String clinicalNotes;
    private String diagnosis;
    private String diagnosisCid;
    private String prescription;
    private String examRequests;
    private String treatmentPlan;
    private String followUpInstructions;
}
