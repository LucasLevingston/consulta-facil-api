package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.commission.ReferralCommissionDTO;

import java.math.BigDecimal;

public interface CreateCommissionUseCase {
    ReferralCommissionDTO execute(String referralId, String paymentId, BigDecimal amount, String referrerId);
}
