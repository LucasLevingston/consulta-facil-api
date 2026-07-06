package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.invoice.InvoiceResponseDTO;
import com.consultafacil.application.port.in.ListInvoicesUseCase;
import com.consultafacil.domain.port.out.InvoiceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListInvoicesService implements ListInvoicesUseCase {

    private final InvoiceRepositoryPort invoiceRepository;
    private final InvoiceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> execute() {
        return invoiceRepository.findAll().stream().map(mapper::toDTO).collect(Collectors.toList());
    }
}
