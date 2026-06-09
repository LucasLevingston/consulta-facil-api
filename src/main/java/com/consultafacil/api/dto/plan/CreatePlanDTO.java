package com.consultafacil.api.dto.plan;

import com.consultafacil.domain.enums.BillingPeriod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreatePlanDTO {

    @NotBlank(message = "slug é obrigatório")
    private String slug;

    @NotBlank(message = "name é obrigatório")
    private String name;

    private String description;

    @NotBlank(message = "tier é obrigatório")
    private String tier;

    @NotNull(message = "billingPeriod é obrigatório")
    private BillingPeriod billingPeriod;

    @NotNull(message = "price é obrigatório")
    @DecimalMin(value = "0.01", message = "price deve ser maior que zero")
    private BigDecimal price;

    private int frequency = 1;
    private String frequencyType = "months";
    private List<String> features;
    private int displayOrder = 0;
}
