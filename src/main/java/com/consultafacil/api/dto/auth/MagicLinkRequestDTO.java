package com.consultafacil.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MagicLinkRequestDTO(
        @NotBlank @Email String email
) {}
