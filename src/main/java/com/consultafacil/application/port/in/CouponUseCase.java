package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.coupon.CouponResponseDTO;
import com.consultafacil.api.dto.coupon.CouponValidationResponseDTO;
import com.consultafacil.api.dto.coupon.CreateCouponDTO;
import com.consultafacil.api.dto.coupon.UpdateCouponDTO;

import java.math.BigDecimal;
import java.util.List;

public interface CouponUseCase {

    CouponResponseDTO createCoupon(CreateCouponDTO dto, String adminUserId);

    List<CouponResponseDTO> listCoupons();

    CouponResponseDTO updateCoupon(String id, UpdateCouponDTO dto);

    CouponValidationResponseDTO validate(String code, String userId, String planId, BigDecimal grossAmount);

    void recordUse(String couponId, String userId, String subscriptionId, BigDecimal discountApplied);
}
