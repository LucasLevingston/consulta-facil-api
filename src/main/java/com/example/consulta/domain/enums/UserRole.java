package com.example.consulta.domain.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    PATIENT("ROLE_PATIENT", "Paciente"),
    PROFESSIONAL("ROLE_PROFESSIONAL", "Profissional de Saúde"),
    ADMIN("ROLE_ADMIN", "Administrador"),
    RECEPTIONIST("ROLE_RECEPTIONIST", "Recepcionista");

    private final String authority;
    private final String description;

    UserRole(String authority, String description) {
        this.authority = authority;
        this.description = description;
    }
}
