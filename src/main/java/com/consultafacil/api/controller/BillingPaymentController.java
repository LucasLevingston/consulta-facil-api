package com.consultafacil.api.controller;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;
import com.consultafacil.api.dto.billing.payment.CreateBillingPaymentDTO;
import com.consultafacil.application.port.in.billing.CancelBillingPaymentUseCase;
import com.consultafacil.application.port.in.billing.CreateBillingPaymentUseCase;
import com.consultafacil.application.port.in.billing.GetBillingPaymentByIdUseCase;
import com.consultafacil.application.port.in.billing.HandleBillingPaymentWebhookUseCase;
import com.consultafacil.application.port.in.billing.ListAllBillingPaymentsUseCase;
import com.consultafacil.application.port.in.billing.ListMyBillingPaymentsUseCase;
import com.consultafacil.application.port.in.billing.RefundBillingPaymentUseCase;
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

    private final CreateBillingPaymentUseCase createPayment;
    private final ListMyBillingPaymentsUseCase listMyPayments;
    private final GetBillingPaymentByIdUseCase getById;
    private final CancelBillingPaymentUseCase cancelPayment;
    private final RefundBillingPaymentUseCase refundPayment;
    private final ListAllBillingPaymentsUseCase listAll;
    private final HandleBillingPaymentWebhookUseCase handleWebhook;

    @PostMapping("/billing/payments")
    public ResponseEntity<BillingPaymentResponseDTO> createPayment(@Valid @RequestBody CreateBillingPaymentDTO dto) {
        return ResponseEntity.ok(createPayment.execute(dto));
    }

    @GetMapping("/billing/payments/me")
    public ResponseEntity<List<BillingPaymentResponseDTO>> myPayments(@RequestParam String payerId) {
        return ResponseEntity.ok(listMyPayments.execute(payerId));
    }

    @GetMapping("/billing/payments/{id}")
    public ResponseEntity<BillingPaymentResponseDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(getById.execute(id));
    }

    @PostMapping("/billing/payments/{id}/cancel")
    public ResponseEntity<BillingPaymentResponseDTO> cancel(@PathVariable String id) {
        return ResponseEntity.ok(cancelPayment.execute(id));
    }

    @PostMapping("/billing/payments/{id}/refund")
    public ResponseEntity<BillingPaymentResponseDTO> refund(@PathVariable String id) {
        return ResponseEntity.ok(refundPayment.execute(id));
    }

    @GetMapping("/admin/billing/payments")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<List<BillingPaymentResponseDTO>> listAll() {
        return ResponseEntity.ok(listAll.execute());
    }

    @PostMapping("/billing/payments/webhook")
    public ResponseEntity<Void> webhook(@RequestBody Map<String, Object> payload) {
        String gatewayPaymentId = String.valueOf(payload.getOrDefault("gateway_payment_id", ""));
        String status = String.valueOf(payload.getOrDefault("status", "PAID"));
        handleWebhook.execute(gatewayPaymentId, status);
        return ResponseEntity.ok().build();
    }
}
