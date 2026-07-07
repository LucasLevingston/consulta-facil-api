package com.consultafacil.api.dto.dependent;

import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.RelationshipType;

import java.time.LocalDate;

public record UpdateDependentDTO(
        String name,
        String cpf,
        LocalDate birthDate,
        Gender gender,
        RelationshipType relationship
) {}
