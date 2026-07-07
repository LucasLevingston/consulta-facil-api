package com.consultafacil.domain.port.out.coupon;

import com.consultafacil.domain.entity.Coupon;

import java.util.List;
import java.util.Optional;

public interface CouponRepositoryPort {
    Coupon save(Coupon coupon);
    Optional<Coupon> findById(String id);
    Optional<Coupon> findByCodeIgnoreCase(String code);
    List<Coupon> findAll();
    int incrementCurrentUses(String id);
}
