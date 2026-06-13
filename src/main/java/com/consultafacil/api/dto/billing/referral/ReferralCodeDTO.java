package com.consultafacil.api.dto.billing.referral;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralCodeDTO {
    private String id;
    private String userId;
    private String code;
    private boolean active;
    private LocalDateTime createdAt;
}
