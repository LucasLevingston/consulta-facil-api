package com.consultafacil.application.service.coupon;

import com.consultafacil.api.dto.billing.coupon.CouponUsageResponseDTO;
import com.consultafacil.api.dto.billing.coupon.CouponValidationResultDTO;
import com.consultafacil.application.port.in.ApplyCouponUseCase;
import com.consultafacil.application.port.in.ValidateCouponUsageUseCase;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.entity.CouponUsage;
import com.consultafacil.domain.port.out.CouponRepositoryPort;
import com.consultafacil.domain.port.out.CouponUsageRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ApplyCouponService implements ApplyCouponUseCase {

    private final CouponRepositoryPort couponRepository;
    private final CouponUsageRepositoryPort couponUsageRepository;
    private final ValidateCouponUsageUseCase validateCouponUsageUseCase;
    private final CouponUsageMapper mapper;

    @Override
    @Transactional
    public CouponUsageResponseDTO execute(String code, String userId, String paymentId, BigDecimal amount) {
        CouponValidationResultDTO result = validateCouponUsageUseCase.execute(code, userId, amount);
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

        return mapper.toDTO(couponUsageRepository.save(usage));
    }
}
