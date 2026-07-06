package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.invoice.InvoiceResponseDTO;
import com.consultafacil.application.port.in.GetInvoiceByPaymentIdUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.port.out.InvoiceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetInvoiceByPaymentIdService implements GetInvoiceByPaymentIdUseCase {

    private final InvoiceRepositoryPort invoiceRepository;
    private final InvoiceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponseDTO execute(String paymentId) {
        return invoiceRepository.findByPaymentId(paymentId)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", paymentId));
    }
}
