package com.consultafacil.api.dto.coupon;

import com.consultafacil.domain.enums.CouponType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateCouponDTO {

    @NotBlank(message = "code é obrigatório")
    private String code;

    private String description;

    @NotNull(message = "type é obrigatório")
    private CouponType type;

    @NotNull(message = "value é obrigatório")
    @DecimalMin(value = "0.01", message = "value deve ser maior que zero")
    private BigDecimal value;

    private Integer maxUses;

    private Integer maxUsesPerUser = 1;

    private LocalDateTime startsAt;

    private LocalDateTime expiresAt;

    private String applicablePlanIds;

    private String sellerId;
}
