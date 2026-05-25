package com.example.consulta.api.dto.professionalservice;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfessionalServiceDTO {
    private String name;
    private String description;

    @Positive(message = "Preço deve ser positivo")
    private BigDecimal price;

    @Min(value = 1, message = "Duração mínima é 1 minuto")
    private Integer durationMinutes;

    private Boolean requiresConsultation;
}
