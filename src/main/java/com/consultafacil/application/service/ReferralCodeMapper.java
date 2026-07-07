package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.referral.ReferralCodeDTO;
import com.consultafacil.domain.entity.ReferralCode;
import org.springframework.stereotype.Component;

@Component
public class ReferralCodeMapper {

    public ReferralCodeDTO toDTO(ReferralCode rc) {
        return ReferralCodeDTO.builder()
                .id(rc.getId())
                .userId(rc.getUserId())
                .code(rc.getCode())
                .active(rc.isActive())
                .createdAt(rc.getCreatedAt())
                .build();
    }
}
