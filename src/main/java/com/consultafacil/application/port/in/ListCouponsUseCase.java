package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.coupon.CouponResponseDTO;

import java.util.List;

public interface ListCouponsUseCase {
    List<CouponResponseDTO> execute();
}
