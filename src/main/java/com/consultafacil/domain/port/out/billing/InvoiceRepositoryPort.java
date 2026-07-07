package com.consultafacil.domain.port.out.billing;

import com.consultafacil.domain.entity.Invoice;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepositoryPort {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(String id);
    Optional<Invoice> findByPaymentId(String paymentId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findAll();
}
