package com.consultafacil.api.controller;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;
import com.consultafacil.api.dto.billing.payment.CreateBillingPaymentDTO;
import com.consultafacil.application.port.in.BillingPaymentUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BillingPaymentController {

    private final BillingPaymentUseCase billingPaymentUseCase;

    @PostMapping("/billing/payments")
    public ResponseEntity<BillingPaymentResponseDTO> createPayment(@Valid @RequestBody CreateBillingPaymentDTO dto) {
        return ResponseEntity.ok(billingPaymentUseCase.createPayment(dto));
    }

    @GetMapping("/billing/payments/me")
    public ResponseEntity<List<BillingPaymentResponseDTO>> myPayments(@RequestParam String payerId) {
        return ResponseEntity.ok(billingPaymentUseCase.listMyPayments(payerId));
    }

    @GetMapping("/billing/payments/{id}")
    public ResponseEntity<BillingPaymentResponseDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(billingPaymentUseCase.getById(id));
    }

    @PostMapping("/billing/payments/{id}/cancel")
    public ResponseEntity<BillingPaymentResponseDTO> cancel(@PathVariable String id) {
        return ResponseEntity.ok(billingPaymentUseCase.cancelPayment(id));
    }

    @PostMapping("/billing/payments/{id}/refund")
    public ResponseEntity<BillingPaymentResponseDTO> refund(@PathVariable String id) {
        return ResponseEntity.ok(billingPaymentUseCase.refundPayment(id));
    }

    @GetMapping("/admin/billing/payments")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<List<BillingPaymentResponseDTO>> listAll() {
        return ResponseEntity.ok(billingPaymentUseCase.listAll());
    }

    @PostMapping("/billing/payments/webhook")
    public ResponseEntity<Void> webhook(@RequestBody Map<String, Object> payload) {
        String gatewayPaymentId = String.valueOf(payload.getOrDefault("gateway_payment_id", ""));
        String status = String.valueOf(payload.getOrDefault("status", "PAID"));
        billingPaymentUseCase.handleWebhook(gatewayPaymentId, status);
        return ResponseEntity.ok().build();
    }
}
