package com.consultafacil.api.controller;

import com.consultafacil.api.dto.billing.invoice.InvoiceResponseDTO;
import com.consultafacil.application.port.in.InvoiceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceUseCase invoiceUseCase;

    @GetMapping("/admin/billing/invoices")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<List<InvoiceResponseDTO>> listAll() {
        return ResponseEntity.ok(invoiceUseCase.listAll());
    }

    @GetMapping("/admin/billing/invoices/{id}")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<InvoiceResponseDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(invoiceUseCase.getById(id));
    }

    @GetMapping("/billing/invoices/by-payment/{paymentId}")
    public ResponseEntity<InvoiceResponseDTO> getByPaymentId(@PathVariable String paymentId) {
        return ResponseEntity.ok(invoiceUseCase.getByPaymentId(paymentId));
    }
}
