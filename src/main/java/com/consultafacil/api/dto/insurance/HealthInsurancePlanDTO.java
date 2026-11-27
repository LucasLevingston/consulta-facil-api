package com.consultafacil.api.dto.insurance;

public record HealthInsurancePlanDTO(
        String id,
        String name,
        String code,
        boolean active
) {}
