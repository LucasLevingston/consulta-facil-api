package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.coupon.CouponValidationResultDTO;
import com.consultafacil.application.port.in.ValidateCouponUsageUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.enums.CouponStatus;
import com.consultafacil.domain.enums.CouponType;
import com.consultafacil.domain.port.out.CouponRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ValidateCouponUsageService implements ValidateCouponUsageUseCase {

    private final CouponRepositoryPort couponRepository;

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResultDTO execute(String code, String userId, BigDecimal amount) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", code));

        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            return CouponValidationResultDTO.invalid("Cupom inativo");
        }

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartsAt() != null && now.isBefore(coupon.getStartsAt())) {
            return CouponValidationResultDTO.invalid("Cupom ainda não está ativo");
        }
        if (coupon.getExpiresAt() != null && now.isAfter(coupon.getExpiresAt())) {
            return CouponValidationResultDTO.invalid("Cupom expirado");
        }

        if (coupon.getMaxUses() != null && coupon.getCurrentUses() >= coupon.getMaxUses()) {
            return CouponValidationResultDTO.invalid("Cupom esgotado");
        }

        BigDecimal discount = calculateDiscount(coupon, amount);
        return CouponValidationResultDTO.valid(discount, amount.subtract(discount), coupon.getCode());
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal amount) {
        if (coupon.getType() == CouponType.PERCENT) {
            return amount.multiply(coupon.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return coupon.getValue().min(amount);
    }
}
