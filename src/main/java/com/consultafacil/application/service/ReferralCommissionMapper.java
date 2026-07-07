package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.commission.ReferralCommissionDTO;
import com.consultafacil.domain.entity.ReferralCommission;
import org.springframework.stereotype.Component;

@Component
public class ReferralCommissionMapper {

    public ReferralCommissionDTO toDTO(ReferralCommission c) {
        return ReferralCommissionDTO.builder()
                .id(c.getId())
                .referralId(c.getReferralId())
                .paymentId(c.getPaymentId())
                .amount(c.getAmount())
                .percentage(c.getPercentage())
                .availableAt(c.getAvailableAt())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
