package com.consultafacil.api.dto.appointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientSummaryDTO {
    private String id;
    private String name;
    private LocalDateTime lastAppointment;
    private Long totalAppointments;
}
