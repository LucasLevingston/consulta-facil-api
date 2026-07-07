package com.consultafacil.application.service.billing;

import com.consultafacil.api.dto.billing.invoice.InvoiceResponseDTO;
import com.consultafacil.application.port.in.GetInvoiceByIdUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.port.out.InvoiceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetInvoiceByIdService implements GetInvoiceByIdUseCase {

    private final InvoiceRepositoryPort invoiceRepository;
    private final InvoiceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponseDTO execute(String id) {
        return invoiceRepository.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }
}
