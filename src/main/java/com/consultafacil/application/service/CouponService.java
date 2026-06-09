package com.consultafacil.application.service;

import com.consultafacil.api.dto.coupon.CouponResponseDTO;
import com.consultafacil.api.dto.coupon.CouponValidationResponseDTO;
import com.consultafacil.api.dto.coupon.CreateCouponDTO;
import com.consultafacil.api.dto.coupon.UpdateCouponDTO;
import com.consultafacil.application.port.in.CouponUseCase;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.entity.CouponUse;
import com.consultafacil.domain.enums.CouponStatus;
import com.consultafacil.domain.enums.CouponType;
import com.consultafacil.domain.port.out.CouponRepositoryPort;
import com.consultafacil.domain.port.out.CouponUseRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService implements CouponUseCase {

    private final CouponRepositoryPort couponRepository;
    private final CouponUseRepositoryPort couponUseRepository;

    @Override
    @Transactional
    public CouponResponseDTO createCoupon(CreateCouponDTO dto, String adminUserId) {
        Coupon coupon = Coupon.builder()
                .code(dto.getCode().trim().toUpperCase())
                .description(dto.getDescription())
                .type(dto.getType())
                .value(dto.getValue())
                .maxUses(dto.getMaxUses())
                .maxUsesPerUser(dto.getMaxUsesPerUser() != null ? dto.getMaxUsesPerUser() : 1)
                .startsAt(dto.getStartsAt())
                .expiresAt(dto.getExpiresAt())
                .applicablePlanIds(dto.getApplicablePlanIds())
                .sellerId(dto.getSellerId())
                .status(CouponStatus.ACTIVE)
                .createdBy(adminUserId)
                .build();

        return toDTO(couponRepository.save(coupon));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponseDTO> listCoupons() {
        return couponRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CouponResponseDTO updateCoupon(String id, UpdateCouponDTO dto) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", id));

        if (dto.getDescription() != null) coupon.setDescription(dto.getDescription());
        if (dto.getStatus() != null) coupon.setStatus(dto.getStatus());
        if (dto.getExpiresAt() != null) coupon.setExpiresAt(dto.getExpiresAt());
        if (dto.getMaxUses() != null) coupon.setMaxUses(dto.getMaxUses());

        return toDTO(couponRepository.save(coupon));
    }

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponseDTO validate(String code, String userId, String planId, BigDecimal grossAmount) {
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

    @Override
    @Transactional
    public void recordUse(String couponId, String userId, String subscriptionId, BigDecimal discountApplied) {
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

    // ── Helpers ───────────────────────────────────────────────────────────

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

    private CouponResponseDTO toDTO(Coupon c) {
        return CouponResponseDTO.builder()
                .id(c.getId())
                .code(c.getCode())
                .description(c.getDescription())
                .type(c.getType())
                .value(c.getValue())
                .maxUses(c.getMaxUses())
                .currentUses(c.getCurrentUses())
                .maxUsesPerUser(c.getMaxUsesPerUser())
                .startsAt(c.getStartsAt())
                .expiresAt(c.getExpiresAt())
                .applicablePlanIds(c.getApplicablePlanIds())
                .sellerId(c.getSellerId())
                .status(c.getStatus())
                .createdBy(c.getCreatedBy())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
