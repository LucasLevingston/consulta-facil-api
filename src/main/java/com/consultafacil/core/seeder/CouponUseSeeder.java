package com.consultafacil.core.seeder;

import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.entity.CouponUse;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.enums.CouponType;
import com.consultafacil.domain.repository.coupon.CouponUseRepository;
import com.consultafacil.domain.repository.subscription.SubscriptionRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponUseSeeder {

    private final SubscriptionRepository subscriptionRepository;
    private final CouponUseRepository couponUseRepository;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    public void seed(List<Coupon> coupons) {
        if (coupons.isEmpty()) return;
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        if (subscriptions.isEmpty()) return;

        int created = 0;
        for (int i = 0; i < Math.min(subscriptions.size(), 10); i++) {
            if (faker.random().nextInt(100) < 40) continue;
            try {
                Subscription sub = subscriptions.get(i);
                Coupon coupon = coupons.get(faker.random().nextInt(coupons.size()));
                BigDecimal discount = coupon.getType() == CouponType.PERCENT
                        ? new BigDecimal("99.90").multiply(coupon.getValue())
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                        : coupon.getValue();
                couponUseRepository.save(CouponUse.builder()
                        .coupon(coupon)
                        .userId(sub.getUser().getId())
                        .subscriptionId(sub.getId())
                        .discountApplied(discount)
                        .build());
                created++;
            } catch (Exception e) {
                log.debug("Erro ao criar coupon use: {}", e.getMessage());
            }
        }
        log.info("[Seed] CouponUses criados: {}", created);
    }
}
