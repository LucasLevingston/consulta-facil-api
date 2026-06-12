package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.invoice.InvoiceResponseDTO;
import com.consultafacil.application.port.in.InvoiceUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Invoice;
import com.consultafacil.domain.port.out.InvoiceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService implements InvoiceUseCase {

    private final InvoiceRepositoryPort invoiceRepository;

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponseDTO getById(String id) {
        return invoiceRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponseDTO getByPaymentId(String paymentId) {
        return invoiceRepository.findByPaymentId(paymentId)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", paymentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> listAll() {
        return invoiceRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private InvoiceResponseDTO toDTO(Invoice i) {
        return InvoiceResponseDTO.builder()
                .id(i.getId())
                .paymentId(i.getPayment().getId())
                .invoiceNumber(i.getInvoiceNumber())
                .pdfUrl(i.getPdfUrl())
                .hostedUrl(i.getHostedUrl())
                .createdAt(i.getCreatedAt())
                .build();
    }
}
