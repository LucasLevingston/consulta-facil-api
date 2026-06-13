package com.consultafacil.api.dto.billing.coupon;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponUsageResponseDTO {
    private String id;
    private String couponId;
    private String couponCode;
    private String userId;
    private String paymentId;
    private BigDecimal discountAmount;
    private LocalDateTime usedAt;
}
