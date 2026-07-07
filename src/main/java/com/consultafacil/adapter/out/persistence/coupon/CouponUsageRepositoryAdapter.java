package com.consultafacil.adapter.out.persistence.coupon;

import com.consultafacil.domain.entity.CouponUsage;
import com.consultafacil.domain.port.out.coupon.CouponUsageRepositoryPort;
import com.consultafacil.domain.repository.coupon.CouponUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CouponUsageRepositoryAdapter implements CouponUsageRepositoryPort {

    private final CouponUsageRepository couponUsageRepository;

    @Override
    public CouponUsage save(CouponUsage couponUsage) {
        return couponUsageRepository.save(couponUsage);
    }

    @Override
    public List<CouponUsage> findByCouponId(String couponId) {
        return couponUsageRepository.findByCouponId(couponId);
    }

    @Override
    public boolean existsByUserIdAndCouponId(String userId, String couponId) {
        return couponUsageRepository.existsByUserIdAndCouponId(userId, couponId);
    }

    @Override
    public long countByCouponId(String couponId) {
        return couponUsageRepository.countByCouponId(couponId);
    }

    @Override
    public List<CouponUsage> findByUserId(String userId) {
        return couponUsageRepository.findByUserId(userId);
    }

    @Override
    public List<CouponUsage> findAll() {
        return couponUsageRepository.findAll();
    }
}
