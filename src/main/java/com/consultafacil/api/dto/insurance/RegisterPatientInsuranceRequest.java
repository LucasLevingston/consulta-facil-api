package com.consultafacil.api.dto.insurance;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record RegisterPatientInsuranceRequest(
        @NotBlank String planId,
        @NotBlank String memberNumber,
        @NotBlank String holderName,
        LocalDate validUntil
) {}
