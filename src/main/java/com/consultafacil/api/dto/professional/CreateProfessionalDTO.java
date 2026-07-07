package com.consultafacil.api.dto.professional;

import com.consultafacil.domain.enums.ProfessionalType;
import com.consultafacil.domain.enums.Specialty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "Profissão é obrigatória")
    private ProfessionalType profession;

    @NotNull(message = "Especialidade é obrigatória")
    private Specialty specialty;

    @NotBlank(message = "Número de registro é obrigatório")
    @Size(min = 5, max = 50, message = "Número de registro deve ter entre 5 e 50 caracteres")
    private String licenseNumber;
}
