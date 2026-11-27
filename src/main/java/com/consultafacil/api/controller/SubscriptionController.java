package com.consultafacil.api.controller;

import com.consultafacil.api.dto.subscription.CheckoutResponseDTO;
import com.consultafacil.api.dto.subscription.CreateCheckoutDTO;
import com.consultafacil.api.dto.subscription.SubscriptionPlanInfoDTO;
import com.consultafacil.api.dto.subscription.SubscriptionResponseDTO;
import com.consultafacil.application.port.in.subscription.CancelSubscriptionUseCase;
import com.consultafacil.application.port.in.subscription.CreateCheckoutUseCase;
import com.consultafacil.application.port.in.subscription.GetMySubscriptionUseCase;
import com.consultafacil.application.port.in.subscription.HandlePaymentApprovedUseCase;
import com.consultafacil.application.port.in.subscription.HandlePreapprovalWebhookUseCase;
import com.consultafacil.application.port.in.subscription.ListSubscriptionPlansUseCase;
import com.consultafacil.core.security.CustomUserDetails;
import com.consultafacil.core.security.MercadoPagoWebhookValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Subscription management via MercadoPago")
public class SubscriptionController {

    private final CreateCheckoutUseCase createCheckout;
    private final GetMySubscriptionUseCase getMySubscription;
    private final HandlePaymentApprovedUseCase handlePaymentApproved;
    private final HandlePreapprovalWebhookUseCase handlePreapprovalWebhook;
    private final MercadoPagoWebhookValidator webhookValidator;
    private final ListSubscriptionPlansUseCase listPlans;
    private final CancelSubscriptionUseCase cancelSubscription;

    @PostMapping("/checkout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create MercadoPago checkout preference")
    public ResponseEntity<CheckoutResponseDTO> createCheckout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateCheckoutDTO dto) {
        return ResponseEntity.ok(createCheckout.execute(userDetails.getUserId(), dto.getPlanId(), dto.getReferralSlug(), dto.getCouponCode()));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user subscription")
    public ResponseEntity<SubscriptionResponseDTO> getMySubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return getMySubscription.execute(userDetails.getUserId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/plans")
    @Operation(summary = "List available subscription plans (public)")
    public ResponseEntity<List<SubscriptionPlanInfoDTO>> listPlans() {
        return ResponseEntity.ok(listPlans.execute());
    }

    @PostMapping("/cancel")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cancel subscription at period end")
    public ResponseEntity<Void> cancelSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        cancelSubscription.execute(userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/webhook")
    @Operation(summary = "MercadoPago subscription webhook (public)")
    public ResponseEntity<Void> webhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false, defaultValue = "") String xRequestId) {
        try {
            String type = (String) payload.get("type");
            Object dataObj = payload.get("data");
            if (!(dataObj instanceof Map<?, ?> data)) return ResponseEntity.ok().build();

            String resourceId = String.valueOf(data.get("id"));

            if ("payment".equals(type)) {
                webhookValidator.validate(resourceId, xRequestId, xSignature);
                handlePaymentApproved.execute(resourceId);
            } else if ("subscription_preapproval".equals(type)) {
                webhookValidator.validate(resourceId, xRequestId, xSignature);
                handlePreapprovalWebhook.execute(resourceId);
            } else {
                return ResponseEntity.ok().build();
            }

        } catch (com.consultafacil.core.exception.WebhookAuthenticationException e) {
            log.warn("[SubscriptionWebhook] Invalid signature: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("[SubscriptionWebhook] Processing error: {}", e.getMessage());
            // Webhooks must always return 200 for non-auth errors
        }
        return ResponseEntity.ok().build();
    }
}
