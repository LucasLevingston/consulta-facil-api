package com.example.consulta.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordDTO(
        @NotBlank String token,
        @NotBlank @Size(min = 8, message = "Senha deve ter ao menos 8 caracteres") String newPassword
) {}
