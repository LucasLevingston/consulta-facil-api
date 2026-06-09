package com.consultafacil.api.dto.coupon;

import com.consultafacil.domain.enums.CouponStatus;
import com.consultafacil.domain.enums.CouponType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CouponResponseDTO {
    private String id;
    private String code;
    private String description;
    private CouponType type;
    private BigDecimal value;
    private Integer maxUses;
    private int currentUses;
    private int maxUsesPerUser;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private String applicablePlanIds;
    private String sellerId;
    private CouponStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
}
