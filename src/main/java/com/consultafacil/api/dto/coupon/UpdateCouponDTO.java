package com.consultafacil.api.dto.coupon;

import com.consultafacil.domain.enums.CouponStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateCouponDTO {
    private String description;
    private CouponStatus status;
    private LocalDateTime expiresAt;
    private Integer maxUses;
}
