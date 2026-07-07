package com.consultafacil.api.dto.professional;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProfessionalExperienceDTO(
        String id,
        @NotBlank String position,
        @NotBlank String institution,
        @NotNull @Min(1900) Integer startYear,
        Integer endYear,
        @Size(max = 500) String description
) {}
