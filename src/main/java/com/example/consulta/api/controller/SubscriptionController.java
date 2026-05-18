package com.example.consulta.api.controller;

import com.example.consulta.api.dto.subscription.CheckoutResponseDTO;
import com.example.consulta.api.dto.subscription.CreateCheckoutDTO;
import com.example.consulta.api.dto.subscription.SubscriptionResponseDTO;
import com.example.consulta.application.service.SubscriptionService;
import com.example.consulta.core.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Subscription management via MercadoPago")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/checkout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create MercadoPago checkout preference")
    public ResponseEntity<CheckoutResponseDTO> createCheckout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateCheckoutDTO dto) {
        CheckoutResponseDTO response = subscriptionService.createCheckout(
                userDetails.getUserId(), dto.getPlanId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user subscription")
    public ResponseEntity<SubscriptionResponseDTO> getMySubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return subscriptionService.getMySubscription(userDetails.getUserId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/webhook")
    @Operation(summary = "MercadoPago webhook — no auth required")
    public ResponseEntity<Void> webhook(@RequestBody Map<String, Object> payload) {
        try {
            String type = (String) payload.get("type");
            if ("payment".equals(type)) {
                Object dataObj = payload.get("data");
                if (dataObj instanceof Map<?, ?> data) {
                    String paymentId = String.valueOf(data.get("id"));
                    // External reference format: "userId|planId"
                    // We parse it in the service after fetching payment details from MP
                    subscriptionService.handlePaymentApproved(paymentId, null);
                }
            }
        } catch (Exception e) {
            // Webhooks must always return 200
        }
        return ResponseEntity.ok().build();
    }
}
