package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.port.out.CouponRepositoryPort;
import com.consultafacil.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CouponRepositoryAdapter implements CouponRepositoryPort {

    private final CouponRepository couponRepository;

    @Override
    public Coupon save(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    @Override
    public Optional<Coupon> findById(String id) {
        return couponRepository.findById(id);
    }

    @Override
    public Optional<Coupon> findByCodeIgnoreCase(String code) {
        return couponRepository.findByCodeIgnoreCase(code);
    }

    @Override
    public List<Coupon> findAll() {
        return couponRepository.findAll();
    }

    @Override
    public int incrementCurrentUses(String id) {
        return couponRepository.incrementCurrentUses(id);
    }
}
