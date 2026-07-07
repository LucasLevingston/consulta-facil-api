package com.consultafacil.domain.port.out.coupon;

import com.consultafacil.domain.entity.CouponUse;

import java.util.List;

public interface CouponUseRepositoryPort {
    CouponUse save(CouponUse couponUse);
    long countByUserIdAndCouponId(String userId, String couponId);
    List<CouponUse> findByCouponId(String couponId);
}
