package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;

public interface RefundBillingPaymentUseCase {
    BillingPaymentResponseDTO execute(String id);
}
