package com.consultafacil.api.dto.procedurerequest;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProcedureRequestDTO {

    @NotBlank(message = "ID do serviço é obrigatório")
    private String serviceId;

    @NotBlank(message = "ID do paciente é obrigatório")
    private String patientId;

    private String notes;
}
