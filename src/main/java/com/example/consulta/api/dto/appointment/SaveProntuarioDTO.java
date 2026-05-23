package com.example.consulta.api.dto.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveProntuarioDTO {
    private String clinicalNotes;
    private String diagnosis;
    private String diagnosisCid;
    private String prescription;
    private String examRequests;
    private String treatmentPlan;
    private String followUpInstructions;
}
