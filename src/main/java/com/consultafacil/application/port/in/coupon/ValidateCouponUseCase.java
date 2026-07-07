package com.consultafacil.application.port.in.coupon;

import com.consultafacil.api.dto.coupon.CouponValidationResponseDTO;

import java.math.BigDecimal;

public interface ValidateCouponUseCase {
    CouponValidationResponseDTO execute(String code, String userId, String planId, BigDecimal grossAmount);
}
