package com.consultafacil.application.port.in.coupon;

import com.consultafacil.api.dto.coupon.CouponResponseDTO;
import com.consultafacil.api.dto.coupon.UpdateCouponDTO;

public interface UpdateCouponUseCase {
    CouponResponseDTO execute(String id, UpdateCouponDTO dto);
}
