package com.consultafacil.application.service.coupon;

import com.consultafacil.api.dto.coupon.CouponResponseDTO;
import com.consultafacil.api.dto.coupon.CreateCouponDTO;
import com.consultafacil.application.port.in.coupon.CreateCouponUseCase;
import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.enums.CouponStatus;
import com.consultafacil.domain.port.out.coupon.CouponRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCouponService implements CreateCouponUseCase {

    private final CouponRepositoryPort couponRepository;
    private final CouponMapper mapper;

    @Override
    @Transactional
    public CouponResponseDTO execute(CreateCouponDTO dto, String adminUserId) {
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

        return mapper.toDTO(couponRepository.save(coupon));
    }
}
