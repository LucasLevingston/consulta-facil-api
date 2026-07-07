package com.consultafacil.api.controller;

import com.consultafacil.api.dto.billing.invoice.InvoiceResponseDTO;
import com.consultafacil.application.port.in.billing.GetInvoiceByIdUseCase;
import com.consultafacil.application.port.in.billing.GetInvoiceByPaymentIdUseCase;
import com.consultafacil.application.port.in.billing.ListInvoicesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InvoiceController {

    private final ListInvoicesUseCase listInvoicesUseCase;
    private final GetInvoiceByIdUseCase getInvoiceByIdUseCase;
    private final GetInvoiceByPaymentIdUseCase getInvoiceByPaymentIdUseCase;

    @GetMapping("/admin/billing/invoices")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<List<InvoiceResponseDTO>> listAll() {
        return ResponseEntity.ok(listInvoicesUseCase.execute());
    }

    @GetMapping("/admin/billing/invoices/{id}")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<InvoiceResponseDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(getInvoiceByIdUseCase.execute(id));
    }

    @GetMapping("/billing/invoices/by-payment/{paymentId}")
    public ResponseEntity<InvoiceResponseDTO> getByPaymentId(@PathVariable String paymentId) {
        return ResponseEntity.ok(getInvoiceByPaymentIdUseCase.execute(paymentId));
    }
}
