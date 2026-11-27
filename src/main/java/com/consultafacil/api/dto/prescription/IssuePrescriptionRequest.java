package com.consultafacil.api.dto.prescription;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record IssuePrescriptionRequest(
        @NotBlank String appointmentId,
        @NotBlank String medications,
        String instructions,
        LocalDate validUntil
) {}
