package com.consultafacil.application.service.coupon;

import com.consultafacil.application.port.in.coupon.RecordCouponUseUseCase;
import com.consultafacil.domain.entity.CouponUse;
import com.consultafacil.domain.port.out.coupon.CouponRepositoryPort;
import com.consultafacil.domain.port.out.coupon.CouponUseRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordCouponUseService implements RecordCouponUseUseCase {

    private final CouponRepositoryPort couponRepository;
    private final CouponUseRepositoryPort couponUseRepository;

    @Override
    @Transactional
    public void execute(String couponId, String userId, String subscriptionId, BigDecimal discountApplied) {
        couponRepository.findById(couponId).ifPresent(coupon -> {
            int updated = couponRepository.incrementCurrentUses(couponId);
            if (updated == 0) {
                log.warn("[Coupon] Could not increment uses for couponId={} — limit reached or not found", couponId);
            }

            CouponUse use = CouponUse.builder()
                    .coupon(coupon)
                    .userId(userId)
                    .subscriptionId(subscriptionId)
                    .discountApplied(discountApplied != null ? discountApplied : BigDecimal.ZERO)
                    .build();
            couponUseRepository.save(use);

            log.info("[Coupon] Use recorded for couponId={} userId={} subscriptionId={}",
                    couponId, userId, subscriptionId);
        });
    }
}
