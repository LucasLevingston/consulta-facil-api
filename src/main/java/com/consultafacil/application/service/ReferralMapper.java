package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.referral.ReferralDTO;
import com.consultafacil.domain.entity.Referral;
import org.springframework.stereotype.Component;

@Component
public class ReferralMapper {

    public ReferralDTO toDTO(Referral r) {
        return ReferralDTO.builder()
                .id(r.getId())
                .referrerId(r.getReferrerId())
                .referredId(r.getReferredId())
                .referralCodeId(r.getReferralCodeId())
                .firstPaymentId(r.getFirstPaymentId())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
