package com.consultafacil.application.port.in.billing;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;
import com.consultafacil.api.dto.billing.payment.CreateBillingPaymentDTO;

public interface CreateBillingPaymentUseCase {
    BillingPaymentResponseDTO execute(CreateBillingPaymentDTO dto);
}
