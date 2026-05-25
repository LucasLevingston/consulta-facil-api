package com.example.consulta.api.dto.procedurerequest;

import com.example.consulta.domain.enums.AppointmentModality;
import jakarta.validation.constraints.Future;
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
public class ScheduleProcedureRequestDTO {

    @NotNull(message = "Data e hora são obrigatórias")
    @Future(message = "A data deve ser no futuro")
    private LocalDateTime scheduledAt;

    private AppointmentModality modality;
}
