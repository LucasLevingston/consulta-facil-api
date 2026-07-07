package com.consultafacil.api.dto.billing.commission;

import com.consultafacil.domain.enums.CommissionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralCommissionDTO {
    private String id;
    private String referralId;
    private String paymentId;
    private BigDecimal amount;
    private BigDecimal percentage;
    private LocalDateTime availableAt;
    private CommissionStatus status;
    private LocalDateTime createdAt;
}
