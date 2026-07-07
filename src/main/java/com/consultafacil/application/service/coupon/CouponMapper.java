package com.consultafacil.application.service.coupon;

import com.consultafacil.api.dto.coupon.CouponResponseDTO;
import com.consultafacil.domain.entity.Coupon;
import org.springframework.stereotype.Component;

@Component
public class CouponMapper {

    public CouponResponseDTO toDTO(Coupon c) {
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
