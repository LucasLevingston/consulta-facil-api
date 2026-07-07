package com.consultafacil.domain.repository.coupon;

import com.consultafacil.domain.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, String> {
    boolean existsByUserIdAndCouponId(String userId, String couponId);
    List<CouponUsage> findByCouponId(String couponId);
    long countByCouponId(String couponId);
    List<CouponUsage> findByUserId(String userId);
}
