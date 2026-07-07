package com.consultafacil.application.service.coupon;

import com.consultafacil.api.dto.coupon.CouponValidationResponseDTO;
import com.consultafacil.application.port.in.ValidateCouponUseCase;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.enums.CouponStatus;
import com.consultafacil.domain.enums.CouponType;
import com.consultafacil.domain.port.out.CouponRepositoryPort;
import com.consultafacil.domain.port.out.CouponUseRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ValidateCouponService implements ValidateCouponUseCase {

    private final CouponRepositoryPort couponRepository;
    private final CouponUseRepositoryPort couponUseRepository;

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponseDTO execute(String code, String userId, String planId, BigDecimal grossAmount) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon com código", code));

        assertCouponUsable(coupon, userId, planId);

        BigDecimal gross = grossAmount != null ? grossAmount : BigDecimal.ZERO;
        BigDecimal discount = calculateDiscount(coupon, gross);
        BigDecimal finalPrice = gross.subtract(discount).max(BigDecimal.ZERO);

        return CouponValidationResponseDTO.builder()
                .couponId(coupon.getId())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .type(coupon.getType())
                .value(coupon.getValue())
                .grossAmount(gross)
                .discountAmount(discount)
                .finalPrice(finalPrice)
                .build();
    }

    private void assertCouponUsable(Coupon coupon, String userId, String planId) {
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new BadRequestException("Cupom inativo ou expirado");
        }

        LocalDateTime now = LocalDateTime.now();

        if (coupon.getStartsAt() != null && now.isBefore(coupon.getStartsAt())) {
            throw new BadRequestException("Cupom ainda não está disponível");
        }

        if (coupon.getExpiresAt() != null && now.isAfter(coupon.getExpiresAt())) {
            throw new BadRequestException("Cupom expirado");
        }

        if (coupon.getMaxUses() != null && coupon.getCurrentUses() >= coupon.getMaxUses()) {
            throw new BadRequestException("Cupom esgotado");
        }

        long userUses = couponUseRepository.countByUserIdAndCouponId(userId, coupon.getId());
        if (userUses >= coupon.getMaxUsesPerUser()) {
            throw new BadRequestException("Você já utilizou este cupom o número máximo de vezes permitido");
        }

        if (coupon.getApplicablePlanIds() != null && !coupon.getApplicablePlanIds().isBlank()) {
            List<String> allowedPlans = Arrays.stream(coupon.getApplicablePlanIds().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            if (!allowedPlans.contains(planId)) {
                throw new BadRequestException("Cupom não aplicável ao plano selecionado");
            }
        }
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal grossAmount) {
        if (coupon.getType() == CouponType.PERCENT) {
            return grossAmount.multiply(coupon.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return coupon.getValue().min(grossAmount).setScale(2, RoundingMode.HALF_UP);
    }
}
