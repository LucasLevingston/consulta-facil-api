package com.consultafacil.api.dto.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientVaccineDTO(
        String id,
        @NotBlank String vaccineName,
        String doseNumber,
        LocalDate administeredAt,
        @Size(max = 500) String notes
) {}
