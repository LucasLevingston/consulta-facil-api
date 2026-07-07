package com.consultafacil.application.port.in.billing;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;

public interface GetBillingPaymentByIdUseCase {
    BillingPaymentResponseDTO execute(String id);
}
