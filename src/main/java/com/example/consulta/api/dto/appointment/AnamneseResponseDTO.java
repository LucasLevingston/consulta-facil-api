package com.example.consulta.api.dto.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnamneseResponseDTO {
    private String id;
    private String appointmentId;
    private String chiefComplaint;
    private String currentMedications;
    private String allergies;
    private String medicalHistory;
    private String familyHistory;
    private String observations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
