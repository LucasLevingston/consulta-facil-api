package com.consultafacil.api.dto.appointment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelAppointmentDTO {
    @NotBlank(message = "Motivo do cancelamento é obrigatório")
    private String cancellationReason;
}
