package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.coupon.CouponUsageResponseDTO;

import java.util.List;

public interface GetCouponUsagesByCouponIdUseCase {
    List<CouponUsageResponseDTO> execute(String couponId);
}
