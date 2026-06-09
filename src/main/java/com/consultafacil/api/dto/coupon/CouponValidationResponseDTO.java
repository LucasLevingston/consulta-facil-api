package com.consultafacil.api.dto.coupon;

import com.consultafacil.domain.enums.CouponType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CouponValidationResponseDTO {
    private String couponId;
    private String code;
    private String description;
    private CouponType type;
    private BigDecimal value;
    private BigDecimal grossAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;
}
