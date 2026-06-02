package com.example.consulta.api.controller;

import com.example.consulta.api.dto.appointment.PaymentResponseDTO;
import com.example.consulta.application.port.in.CreateAppointmentPaymentUseCase;
import com.example.consulta.application.port.in.HandlePaymentWebhookUseCase;
import com.example.consulta.core.security.CustomUserDetails;
import com.example.consulta.core.security.MercadoPagoWebhookValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Appointment payment endpoints")
public class PaymentController {

    private final CreateAppointmentPaymentUseCase createPayment;
    private final HandlePaymentWebhookUseCase handleWebhook;
    private final MercadoPagoWebhookValidator webhookValidator;

    @PostMapping("/appointments/{appointmentId}/payment")
    @PreAuthorize("hasRole('PATIENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create payment preference for an appointment")
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @PathVariable String appointmentId,
            @RequestParam(required = false) BigDecimal amount,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(createPayment.execute(appointmentId, userDetails.getUserId(), amount));
    }

    @PostMapping("/payments/webhook")
    @Operation(summary = "MercadoPago payment webhook (public)")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false, defaultValue = "") String xRequestId) {
        String dataId = extractDataId(body);
        webhookValidator.validate(dataId, xRequestId, xSignature);
        handleWebhook.execute(body);
        return ResponseEntity.ok().build();
    }

    private String extractDataId(Map<String, Object> body) {
        Object dataObj = body.get("data");
        if (dataObj instanceof Map<?, ?> data) {
            Object id = data.get("id");
            return id != null ? String.valueOf(id) : "";
        }
        return "";
    }
}
