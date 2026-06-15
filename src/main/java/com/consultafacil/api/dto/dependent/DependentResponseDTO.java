package com.consultafacil.api.dto.dependent;

import java.time.LocalDate;

public record DependentResponseDTO(
        String id,
        String name,
        String cpf,
        LocalDate birthDate,
        String gender,
        String relationship,
        String createdAt
) {}
