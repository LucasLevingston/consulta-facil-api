package com.consultafacil.api.dto.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicalNoteResponseDTO {
    private String id;
    private String appointmentId;
    private String clinicalNotes;
    private String diagnosis;
    private String diagnosisCid;
    private String prescription;
    private String examRequests;
    private String treatmentPlan;
    private String followUpInstructions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
