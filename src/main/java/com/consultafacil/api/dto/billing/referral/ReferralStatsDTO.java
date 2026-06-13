package com.consultafacil.api.dto.billing.referral;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralStatsDTO {
    private String code;
    private int totalReferred;
    private long pendingCommissions;
    private long availableCommissions;
    private BigDecimal pendingBalance;
    private BigDecimal availableBalance;
}
