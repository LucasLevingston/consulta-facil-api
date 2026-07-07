package com.consultafacil.api.dto.exam;

import com.consultafacil.domain.enums.ExamType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateExamRequestDTO {

    @NotNull(message = "Tipo de exame é obrigatório")
    private ExamType examName;

    private String instructions;
}
