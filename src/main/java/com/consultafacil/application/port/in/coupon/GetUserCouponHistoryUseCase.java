package com.consultafacil.application.port.in.coupon;

import com.consultafacil.api.dto.billing.coupon.CouponUsageResponseDTO;

import java.util.List;

public interface GetUserCouponHistoryUseCase {
    List<CouponUsageResponseDTO> execute(String userId);
}
