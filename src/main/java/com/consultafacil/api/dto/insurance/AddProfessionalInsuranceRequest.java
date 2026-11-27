package com.consultafacil.api.dto.insurance;

import jakarta.validation.constraints.NotBlank;

public record AddProfessionalInsuranceRequest(
        @NotBlank String planId,
        String notes
) {}
