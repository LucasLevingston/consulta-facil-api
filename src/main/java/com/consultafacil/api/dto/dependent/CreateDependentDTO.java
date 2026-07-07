package com.consultafacil.api.dto.dependent;

import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.RelationshipType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateDependentDTO(
        @NotBlank String name,
        String cpf,
        LocalDate birthDate,
        Gender gender,
        @NotNull RelationshipType relationship
) {}
