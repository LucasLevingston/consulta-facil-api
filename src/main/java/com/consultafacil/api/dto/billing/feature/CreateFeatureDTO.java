package com.consultafacil.api.dto.billing.feature;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateFeatureDTO {

    @NotBlank(message = "key é obrigatório")
    private String key;

    @NotBlank(message = "name é obrigatório")
    private String name;

    private String description;
}
