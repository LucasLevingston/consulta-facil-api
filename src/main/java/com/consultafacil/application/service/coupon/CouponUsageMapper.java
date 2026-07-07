package com.consultafacil.application.service.coupon;

import com.consultafacil.api.dto.billing.coupon.CouponUsageResponseDTO;
import com.consultafacil.domain.entity.CouponUsage;
import org.springframework.stereotype.Component;

@Component
public class CouponUsageMapper {

    public CouponUsageResponseDTO toDTO(CouponUsage usage) {
        return CouponUsageResponseDTO.builder()
                .id(usage.getId())
                .couponId(usage.getCoupon().getId())
                .couponCode(usage.getCoupon().getCode())
                .userId(usage.getUserId())
                .paymentId(usage.getPaymentId())
                .discountAmount(usage.getDiscountAmount())
                .usedAt(usage.getUsedAt())
                .build();
    }
}
