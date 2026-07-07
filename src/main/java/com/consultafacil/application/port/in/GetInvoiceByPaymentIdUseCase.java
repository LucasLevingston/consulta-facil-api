package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.invoice.InvoiceResponseDTO;

public interface GetInvoiceByPaymentIdUseCase {

    InvoiceResponseDTO execute(String paymentId);
}
