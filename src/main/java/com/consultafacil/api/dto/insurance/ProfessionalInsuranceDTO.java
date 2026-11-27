package com.consultafacil.api.dto.insurance;

public record ProfessionalInsuranceDTO(
        HealthInsurancePlanDTO plan,
        String notes
) {}
