package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.invoice.InvoiceResponseDTO;

import java.util.List;

public interface InvoiceUseCase {
    InvoiceResponseDTO getById(String id);
    InvoiceResponseDTO getByPaymentId(String paymentId);
    List<InvoiceResponseDTO> listAll();
}
