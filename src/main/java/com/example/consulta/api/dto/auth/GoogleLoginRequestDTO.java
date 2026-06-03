package com.example.consulta.api.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequestDTO(
        @NotBlank String idToken
) {}
