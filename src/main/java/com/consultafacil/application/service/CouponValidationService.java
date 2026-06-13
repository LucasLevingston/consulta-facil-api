package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.coupon.CouponUsageResponseDTO;
import com.consultafacil.api.dto.billing.coupon.CouponValidationResultDTO;
import com.consultafacil.application.port.in.CouponValidationUseCase;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.entity.CouponUsage;
import com.consultafacil.domain.enums.CouponStatus;
import com.consultafacil.domain.enums.CouponType;
import com.consultafacil.domain.port.out.CouponRepositoryPort;
import com.consultafacil.domain.port.out.CouponUsageRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponValidationService implements CouponValidationUseCase {

    private final CouponRepositoryPort couponRepository;
    private final CouponUsageRepositoryPort couponUsageRepository;

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResultDTO validateCoupon(String code, String userId, BigDecimal amount) {
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

    @Override
    @Transactional
    public CouponUsageResponseDTO applyCoupon(String code, String userId, String paymentId, BigDecimal amount) {
        CouponValidationResultDTO result = validateCoupon(code, userId, amount);
        if (!result.isValid()) {
            throw new BadRequestException("Cupom inválido: " + result.getMessage());
        }

        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", code));

        couponRepository.incrementCurrentUses(coupon.getId());

        CouponUsage usage = CouponUsage.builder()
                .coupon(coupon)
                .userId(userId)
                .paymentId(paymentId)
                .discountAmount(result.getDiscountAmount())
                .build();

        return toDTO(couponUsageRepository.save(usage));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponUsageResponseDTO> getUserCouponHistory(String userId) {
        return couponUsageRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponUsageResponseDTO> getAllCouponUsages() {
        return couponUsageRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponUsageResponseDTO> getCouponUsagesByCouponId(String couponId) {
        return couponUsageRepository.findByCouponId(couponId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal amount) {
        if (coupon.getType() == CouponType.PERCENT) {
            return amount.multiply(coupon.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return coupon.getValue().min(amount);
    }

    private CouponUsageResponseDTO toDTO(CouponUsage usage) {
        return CouponUsageResponseDTO.builder()
                .id(usage.getId())
                .couponId(usage.getCoupon().getId())
                .couponCode(usage.getCoupon().getCode())
                .userId(usage.getUserId())
                .paymentId(usage.getPaymentId())
                .discountAmount(usage.getDiscountAmount())
                .usedAt(usage.getUsedAt())
                .build();
    }
}
