package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.coupon.CouponUsageResponseDTO;

import java.util.List;

public interface GetAllCouponUsagesUseCase {
    List<CouponUsageResponseDTO> execute();
}
