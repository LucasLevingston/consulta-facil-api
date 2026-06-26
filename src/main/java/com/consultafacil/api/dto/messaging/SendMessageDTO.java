package com.consultafacil.api.dto.messaging;

import jakarta.validation.constraints.NotBlank;

public record SendMessageDTO(
        @NotBlank String content
) {}
