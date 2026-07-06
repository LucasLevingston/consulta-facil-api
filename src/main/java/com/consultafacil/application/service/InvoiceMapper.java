package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.invoice.InvoiceResponseDTO;
import com.consultafacil.domain.entity.Invoice;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public InvoiceResponseDTO toDTO(Invoice i) {
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
