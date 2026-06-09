package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.CouponUse;
import com.consultafacil.domain.port.out.CouponUseRepositoryPort;
import com.consultafacil.domain.repository.CouponUseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CouponUseRepositoryAdapter implements CouponUseRepositoryPort {

    private final CouponUseRepository couponUseRepository;

    @Override
    public CouponUse save(CouponUse couponUse) {
        return couponUseRepository.save(couponUse);
    }

    @Override
    public long countByUserIdAndCouponId(String userId, String couponId) {
        return couponUseRepository.countByUserIdAndCouponId(userId, couponId);
    }

    @Override
    public List<CouponUse> findByCouponId(String couponId) {
        return couponUseRepository.findByCouponId(couponId);
    }
}
