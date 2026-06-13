package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.commission.ReferralCommissionDTO;

import java.math.BigDecimal;
import java.util.List;

public interface CommissionUseCase {
    ReferralCommissionDTO createCommission(String referralId, String paymentId, BigDecimal amount, String referrerId);
    void cancelCommission(String paymentId);
    void processAvailableCommissions();
    List<ReferralCommissionDTO> getAllCommissions();
    void onPaymentPaid(String paymentId, BigDecimal amount, String payerId);
}
