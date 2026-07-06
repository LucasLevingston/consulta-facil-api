package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.coupon.CouponValidationResultDTO;

import java.math.BigDecimal;

public interface ValidateCouponUsageUseCase {
    CouponValidationResultDTO execute(String code, String userId, BigDecimal amount);
}
