package com.consultafacil.application.port.in.billing;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;

import java.util.List;

public interface ListAllBillingPaymentsUseCase {
    List<BillingPaymentResponseDTO> execute();
}
