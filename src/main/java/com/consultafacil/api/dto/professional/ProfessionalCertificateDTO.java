package com.consultafacil.api.dto.professional;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProfessionalCertificateDTO(
        String id,
        @NotBlank String title,
        String issuingOrganization,
        @Min(1900) Integer issueYear,
        String certificateUrl
) {}
