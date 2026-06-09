package com.consultafacil.api.dto.appointment;

import lombok.Data;

@Data
public class WalkInClinicalNoteDTO {
    private String clinicalNotes;
    private String diagnosis;
    private String diagnosisCid;
    private String prescription;
    private String treatmentPlan;
    private String followUpInstructions;
}
