package com.example.consulta.domain.enums;

public enum AppointmentStatus {
    PENDING("Pendente"),
    CONFIRMED("Confirmada"),
    CHECKED_IN("Check-in realizado"),
    IN_PROGRESS("Em atendimento"),
    CANCELED("Cancelada"),
    COMPLETED("Concluída");

    private final String description;

    AppointmentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
