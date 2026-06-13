package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.coupon.CouponUsageResponseDTO;
import com.consultafacil.api.dto.billing.coupon.CouponValidationResultDTO;

import java.math.BigDecimal;
import java.util.List;

public interface CouponValidationUseCase {
    CouponValidationResultDTO validateCoupon(String code, String userId, BigDecimal amount);
    CouponUsageResponseDTO applyCoupon(String code, String userId, String paymentId, BigDecimal amount);
    List<CouponUsageResponseDTO> getUserCouponHistory(String userId);
    List<CouponUsageResponseDTO> getAllCouponUsages();
    List<CouponUsageResponseDTO> getCouponUsagesByCouponId(String couponId);
}
