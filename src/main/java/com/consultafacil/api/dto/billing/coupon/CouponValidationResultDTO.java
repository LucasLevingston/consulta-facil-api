package com.consultafacil.api.dto.billing.coupon;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidationResultDTO {
    private boolean valid;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String message;
    private String couponCode;

    public static CouponValidationResultDTO invalid(String message) {
        return CouponValidationResultDTO.builder()
                .valid(false)
                .discountAmount(BigDecimal.ZERO)
                .message(message)
                .build();
    }

    public static CouponValidationResultDTO valid(BigDecimal discountAmount, BigDecimal finalAmount, String couponCode) {
        return CouponValidationResultDTO.builder()
                .valid(true)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .couponCode(couponCode)
                .message("Cupom aplicado com sucesso")
                .build();
    }
}
