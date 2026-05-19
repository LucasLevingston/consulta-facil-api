package com.example.consulta.domain.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    PATIENT("ROLE_PATIENT", "Paciente"),
    DOCTOR("ROLE_DOCTOR", "Médico"),
    ADMIN("ROLE_ADMIN", "Administrador");

    private final String authority;
    private final String description;

    UserRole(String authority, String description) {
        this.authority = authority;
        this.description = description;
    }
}
