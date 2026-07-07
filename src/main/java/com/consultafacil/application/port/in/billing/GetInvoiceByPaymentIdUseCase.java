package com.consultafacil.application.port.in.billing;

import com.consultafacil.api.dto.billing.invoice.InvoiceResponseDTO;

public interface GetInvoiceByPaymentIdUseCase {

    InvoiceResponseDTO execute(String paymentId);
}
