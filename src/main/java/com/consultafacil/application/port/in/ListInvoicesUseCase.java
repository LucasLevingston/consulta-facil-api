package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.invoice.InvoiceResponseDTO;

import java.util.List;

public interface ListInvoicesUseCase {

    List<InvoiceResponseDTO> execute();
}
