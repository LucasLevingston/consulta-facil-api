package com.consultafacil.core.security;

import com.consultafacil.core.config.TwilioProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

/**
 * Validates the {@code X-Twilio-Signature} header on incoming Twilio webhook requests.
 *
 * <p>Twilio signs each request using HMAC-SHA1:
 * <ol>
 *   <li>Build a string from the full request URL followed by all POST parameters
 *       sorted alphabetically, each key concatenated directly with its value.</li>
 *   <li>Compute HMAC-SHA1 of that string using {@code TWILIO_AUTH_TOKEN} as the key.</li>
 *   <li>Base64-encode the raw bytes.</li>
 *   <li>Compare to the {@code X-Twilio-Signature} header using constant-time comparison
 *       ({@link MessageDigest#isEqual}) to prevent timing attacks.</li>
 * </ol>
 *
 * <p>Reference: <a href="https://www.twilio.com/docs/usage/webhooks/webhooks-security">
 * Twilio Webhook Security</a>
 *
 * <p>If {@code TWILIO_AUTH_TOKEN} is blank (dev / test mode) validation is skipped
 * and a warning is logged, matching the behaviour of {@link MercadoPagoWebhookValidator}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TwilioWebhookValidator {

    private final TwilioProperties twilioProperties;

    /**
     * Validates the Twilio signature for a form-urlencoded POST webhook request.
     *
     * @param requestUrl      full URL of the webhook endpoint as Twilio sees it
     *                        (scheme + host + path, no query string)
     * @param postParams      all POST parameters received in the request body
     * @param twilioSignature value of the {@code X-Twilio-Signature} header
     * @throws AccessDeniedException if the signature is absent or does not match (HTTP 403)
     */
    public void validate(String requestUrl, Map<String, String> postParams, String twilioSignature) {
        String authToken = twilioProperties.getAuthToken();

        if (authToken == null || authToken.isBlank()) {
            log.warn("TWILIO_AUTH_TOKEN not configured — skipping Twilio webhook signature validation");
            return;
        }

        if (twilioSignature == null || twilioSignature.isBlank()) {
            throw new AccessDeniedException("Missing X-Twilio-Signature header");
        }

        String computed = computeSignature(authToken, requestUrl, postParams);

        byte[] computedBytes = computed.getBytes(StandardCharsets.UTF_8);
        byte[] receivedBytes = twilioSignature.getBytes(StandardCharsets.UTF_8);

        if (!MessageDigest.isEqual(computedBytes, receivedBytes)) {
            throw new AccessDeniedException("Invalid Twilio webhook signature");
        }
    }

    /**
     * Computes the expected Twilio signature.
     *
     * <p>Algorithm:
     * <pre>
     *   manifest = url + sorted(params, key → key + value)
     *   signature = Base64(HMAC-SHA1(authToken, manifest))
     * </pre>
     */
    private String computeSignature(String authToken, String requestUrl, Map<String, String> postParams) {
        StringBuilder manifest = new StringBuilder(requestUrl);

        // TreeMap gives natural (alphabetical) ordering of keys — required by Twilio spec
        new TreeMap<>(postParams).forEach((key, value) -> manifest.append(key).append(value));

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(authToken.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] hash = mac.doFinal(manifest.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC-SHA1 for Twilio webhook", e);
        }
    }
}
