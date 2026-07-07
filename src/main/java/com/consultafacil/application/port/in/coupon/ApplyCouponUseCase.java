package com.consultafacil.application.port.in.coupon;

import com.consultafacil.api.dto.billing.coupon.CouponUsageResponseDTO;

import java.math.BigDecimal;

public interface ApplyCouponUseCase {
    CouponUsageResponseDTO execute(String code, String userId, String paymentId, BigDecimal amount);
}
