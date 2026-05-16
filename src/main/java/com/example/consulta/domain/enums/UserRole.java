package com.example.consulta.domain.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    USER("ROLE_USER", "Usuário comum"),
    ADMIN("ROLE_ADMIN", "Administrador");

    private final String authority;
    private final String description;

    UserRole(String authority, String description) {
        this.authority = authority;
        this.description = description;
    }
}
