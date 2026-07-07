package com.consultafacil.api.dto.professional;

import jakarta.validation.constraints.Size;

public record UpdateBioDTO(
        @Size(max = 1000, message = "Bio must not exceed 1000 characters")
        String bio
) {}
