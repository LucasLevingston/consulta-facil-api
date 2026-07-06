package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.coupon.CouponResponseDTO;
import com.consultafacil.api.dto.coupon.CreateCouponDTO;

public interface CreateCouponUseCase {
    CouponResponseDTO execute(CreateCouponDTO dto, String adminUserId);
}
