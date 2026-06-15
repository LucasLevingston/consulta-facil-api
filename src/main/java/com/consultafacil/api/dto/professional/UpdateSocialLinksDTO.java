package com.consultafacil.api.dto.professional;

import jakarta.validation.constraints.Pattern;

public record UpdateSocialLinksDTO(
        @Pattern(regexp = "^(https?://.*)?$", message = "instagramUrl must be a valid URL")
        String instagramUrl,

        @Pattern(regexp = "^(https?://.*)?$", message = "linkedinUrl must be a valid URL")
        String linkedinUrl,

        @Pattern(regexp = "^(https?://.*)?$", message = "websiteUrl must be a valid URL")
        String websiteUrl,

        @Pattern(regexp = "^(https?://.*)?$", message = "facebookUrl must be a valid URL")
        String facebookUrl
) {}
