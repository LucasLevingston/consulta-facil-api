package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.invoice.InvoiceResponseDTO;

public interface GetInvoiceByIdUseCase {

    InvoiceResponseDTO execute(String id);
}
