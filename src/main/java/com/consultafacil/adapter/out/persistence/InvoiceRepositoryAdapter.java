package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.Invoice;
import com.consultafacil.domain.port.out.InvoiceRepositoryPort;
import com.consultafacil.domain.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InvoiceRepositoryAdapter implements InvoiceRepositoryPort {

    private final InvoiceRepository invoiceRepository;

    @Override
    public Invoice save(Invoice invoice) { return invoiceRepository.save(invoice); }

    @Override
    public Optional<Invoice> findById(String id) { return invoiceRepository.findById(id); }

    @Override
    public Optional<Invoice> findByPaymentId(String paymentId) { return invoiceRepository.findByPaymentId(paymentId); }

    @Override
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) { return invoiceRepository.findByInvoiceNumber(invoiceNumber); }

    @Override
    public List<Invoice> findAll() { return invoiceRepository.findAll(); }
}
