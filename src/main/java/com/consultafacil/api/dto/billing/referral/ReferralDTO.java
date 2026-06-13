package com.consultafacil.api.dto.billing.referral;

import com.consultafacil.domain.enums.ReferralStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralDTO {
    private String id;
    private String referrerId;
    private String referredId;
    private String referralCodeId;
    private String firstPaymentId;
    private ReferralStatus status;
    private LocalDateTime createdAt;
}
