package com.example.consulta.api.dto.appointment;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RescheduleAppointmentDTO {

    @NotNull(message = "New scheduled date is required")
    @Future(message = "Appointment must be scheduled in the future")
    private LocalDateTime scheduledAt;

    private String reason;
}
