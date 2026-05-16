package com.example.consulta.api.dto.appointment;

import com.example.consulta.domain.enums.AppointmentStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAppointmentDTO {
    @NotBlank(message = "ID do médico é obrigatório")
    private String doctorId;

    @NotNull(message = "Data e hora são obrigatórias")
    @Future(message = "A data deve ser no futuro")
    private LocalDateTime scheduledAt;

    private String reason;

    private String notes;
}
