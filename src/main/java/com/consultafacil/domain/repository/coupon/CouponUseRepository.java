package com.consultafacil.domain.repository.coupon;

import com.consultafacil.domain.entity.CouponUse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponUseRepository extends JpaRepository<CouponUse, String> {

    long countByUserIdAndCouponId(String userId, String couponId);

    List<CouponUse> findByCouponId(String couponId);
}
