package com.consultafacil.api.dto.professional;

import com.consultafacil.domain.enums.DegreeType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfessionalEducationDTO(
        String id,
        @NotNull DegreeType degree,
        @NotBlank String institution,
        String fieldOfStudy,
        @Min(1900) @Max(2100) Integer graduationYear
) {}
