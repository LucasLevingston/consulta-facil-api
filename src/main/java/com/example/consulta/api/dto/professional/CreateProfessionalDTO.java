package com.example.consulta.api.dto.professional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProfessionalDTO {
    @NotBlank(message = "Profissão é obrigatória")
    @Size(min = 2, max = 100, message = "Profissão deve ter entre 2 e 100 caracteres")
    private String profession;

    @NotBlank(message = "Especialidade é obrigatória")
    @Size(min = 3, max = 100, message = "Especialidade deve ter entre 3 e 100 caracteres")
    private String specialty;

    @NotBlank(message = "Número de registro é obrigatório")
    @Size(min = 5, max = 50, message = "Número de registro deve ter entre 5 e 50 caracteres")
    private String licenseNumber;
}
