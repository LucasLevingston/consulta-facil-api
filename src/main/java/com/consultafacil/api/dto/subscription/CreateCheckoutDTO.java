package com.consultafacil.api.dto.subscription;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCheckoutDTO {
    @NotBlank(message = "planId é obrigatório")
    private String planId;

    private String referralSlug;
}
