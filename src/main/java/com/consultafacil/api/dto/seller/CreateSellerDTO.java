package com.consultafacil.api.dto.seller;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSellerDTO {

    @NotBlank(message = "userId é obrigatório")
    private String userId;

    @NotNull(message = "commissionRate é obrigatório")
    @DecimalMin(value = "0.01", message = "Comissão mínima é 0.01%")
    @DecimalMax(value = "100.00", message = "Comissão máxima é 100%")
    private BigDecimal commissionRate;

    private String pixKey;

    private String notes;
}
