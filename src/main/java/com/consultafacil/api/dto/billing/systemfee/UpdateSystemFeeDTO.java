package com.consultafacil.api.dto.billing.systemfee;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateSystemFeeDTO {
    private BigDecimal fixedFee;
    private BigDecimal percentageFee;
    private Boolean active;
}
