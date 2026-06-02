package com.example.consulta.domain;

import java.time.LocalDateTime;

public record PatientSummary(
        String id,
        String name,
        LocalDateTime lastAppointment,
        Long totalAppointments
) {}
