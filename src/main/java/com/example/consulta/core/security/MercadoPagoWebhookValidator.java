package com.example.consulta.core.security;

import com.example.consulta.core.exception.WebhookAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@Slf4j
@Component
public class MercadoPagoWebhookValidator {

    private static final long MAX_TIMESTAMP_SKEW_SECONDS = 300;

    @Value("${mercadopago.webhook-secret:}")
    private String webhookSecret;

    /**
     * Validates MercadoPago webhook signature.
     * Spec: https://www.mercadopago.com.br/developers/pt/docs/your-integrations/notifications/webhooks
     *
     * x-signature header format: ts=<epoch>,v1=<hmac_sha256>
     * HMAC manifest: id:<dataId>;request-id:<requestId>;ts:<ts>
     *
     * If webhook-secret is blank, validation is skipped (dev/test mode).
     */
    public void validate(String dataId, String requestId, String xSignature) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("MERCADOPAGO_WEBHOOK_SECRET not configured — skipping signature validation");
            return;
        }

        if (xSignature == null || xSignature.isBlank()) {
            throw new WebhookAuthenticationException("Missing x-signature header");
        }

        String ts = null;
        String v1 = null;
        for (String part : xSignature.split(",")) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length == 2) {
                if ("ts".equals(kv[0])) ts = kv[1];
                if ("v1".equals(kv[0])) v1 = kv[1];
            }
        }

        if (ts == null || v1 == null) {
            throw new WebhookAuthenticationException("Malformed x-signature header");
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(ts);
        } catch (NumberFormatException e) {
            throw new WebhookAuthenticationException("Invalid timestamp in x-signature");
        }

        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - timestamp) > MAX_TIMESTAMP_SKEW_SECONDS) {
            throw new WebhookAuthenticationException("Webhook timestamp expired — possible replay attack");
        }

        String manifest = "id:" + dataId + ";request-id:" + requestId + ";ts:" + ts;
        String computed = hmacSha256(webhookSecret, manifest);

        if (!MessageDigest.isEqual(
                computed.getBytes(StandardCharsets.UTF_8),
                v1.getBytes(StandardCharsets.UTF_8))) {
            throw new WebhookAuthenticationException("Invalid MercadoPago webhook signature");
        }
    }

    private String hmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC-SHA256", e);
        }
    }
}
