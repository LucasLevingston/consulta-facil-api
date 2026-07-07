package com.consultafacil.core.seeder;

import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.enums.CouponStatus;
import com.consultafacil.domain.enums.CouponType;
import com.consultafacil.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponSeeder {

    private final CouponRepository couponRepository;

    private record CouponDef(String code, String desc, CouponType type, BigDecimal value,
            Integer maxUses, String applicablePlans) {
    }

    public List<Coupon> seed(String adminUserId) {
        List<CouponDef> defs = List.of(
                new CouponDef("BEMVINDO10", "10% de desconto para novos usuários",
                        CouponType.PERCENT, new BigDecimal("10.00"), 200, null),
                new CouponDef("PRO50OFF", "R$50 de desconto no plano Pro",
                        CouponType.FIXED, new BigDecimal("50.00"), 100, "plan_pro,plan_pro_annual"),
                new CouponDef("PREMIUM20", "20% de desconto no plano Premium",
                        CouponType.PERCENT, new BigDecimal("20.00"), 50, "plan_premium"),
                new CouponDef("TESTCOUPON", "Cupom de teste ilimitado",
                        CouponType.PERCENT, new BigDecimal("15.00"), null, null));

        List<Coupon> savedCoupons = new ArrayList<>();
        for (CouponDef def : defs) {
            try {
                if (couponRepository.findByCodeIgnoreCase(def.code()).isPresent()) continue;
                Coupon coupon = couponRepository.save(Coupon.builder()
                        .code(def.code())
                        .description(def.desc())
                        .type(def.type())
                        .value(def.value())
                        .maxUses(def.maxUses())
                        .applicablePlanIds(def.applicablePlans())
                        .status(CouponStatus.ACTIVE)
                        .createdBy(adminUserId)
                        .startsAt(LocalDateTime.now().minusDays(30))
                        .expiresAt(LocalDateTime.now().plusMonths(6))
                        .build());
                savedCoupons.add(coupon);
            } catch (Exception e) {
                log.warn("Erro ao criar cupom {}: {}", def.code(), e.getMessage());
            }
        }
        log.info("[Seed] Cupons criados: {}", savedCoupons.size());
        return savedCoupons;
    }
}
