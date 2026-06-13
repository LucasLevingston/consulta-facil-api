package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.CouponUsage;

import java.util.List;

public interface CouponUsageRepositoryPort {
    CouponUsage save(CouponUsage couponUsage);
    List<CouponUsage> findByCouponId(String couponId);
    boolean existsByUserIdAndCouponId(String userId, String couponId);
    long countByCouponId(String couponId);
    List<CouponUsage> findByUserId(String userId);
    List<CouponUsage> findAll();
}
