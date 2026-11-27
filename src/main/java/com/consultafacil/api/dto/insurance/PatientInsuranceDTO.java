package com.consultafacil.api.dto.insurance;

import java.time.LocalDate;

public record PatientInsuranceDTO(
        String id,
        HealthInsurancePlanDTO plan,
        String memberNumber,
        String holderName,
        LocalDate validUntil
) {}
