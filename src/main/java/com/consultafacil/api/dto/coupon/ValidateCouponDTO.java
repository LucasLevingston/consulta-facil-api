package com.consultafacil.api.dto.coupon;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ValidateCouponDTO {

    @NotBlank(message = "code é obrigatório")
    private String code;

    @NotBlank(message = "planId é obrigatório")
    private String planId;

    private BigDecimal grossAmount;
}
