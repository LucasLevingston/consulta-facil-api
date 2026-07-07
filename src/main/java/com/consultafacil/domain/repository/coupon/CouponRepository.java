package com.consultafacil.domain.repository.coupon;

import com.consultafacil.domain.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, String> {

    Optional<Coupon> findByCodeIgnoreCase(String code);

    @Modifying
    @Query("UPDATE Coupon c SET c.currentUses = c.currentUses + 1 WHERE c.id = :id AND (c.maxUses IS NULL OR c.currentUses < c.maxUses)")
    int incrementCurrentUses(@Param("id") String id);
}
