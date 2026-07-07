package com.consultafacil.api.dto.billing.invoice;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InvoiceResponseDTO {
    private String id;
    private String paymentId;
    private String invoiceNumber;
    private String pdfUrl;
    private String hostedUrl;
    private LocalDateTime createdAt;
}
