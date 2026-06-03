package com.example.consulta.core.security;

import com.example.consulta.core.exception.WebhookAuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MercadoPagoWebhookValidatorTest {

    MercadoPagoWebhookValidator validator;
    static final String SECRET = "test-webhook-secret";

    @BeforeEach
    void setUp() {
        validator = new MercadoPagoWebhookValidator();
        ReflectionTestUtils.setField(validator, "webhookSecret", SECRET);
    }

    private String buildSignature(String dataId, String requestId) throws Exception {
        long ts = Instant.now().getEpochSecond();
        String manifest = "id:" + dataId + ";request-id:" + requestId + ";ts:" + ts;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return "ts=" + ts + ",v1=" + hex;
    }

    @Test
    void validate_validSignature_shouldNotThrow() throws Exception {
        String sig = buildSignature("data-1", "req-1");
        assertThatCode(() -> validator.validate("data-1", "req-1", sig)).doesNotThrowAnyException();
    }

    @Test
    void validate_blankSecret_shouldSkipAndNotThrow() {
        ReflectionTestUtils.setField(validator, "webhookSecret", "");
        assertThatCode(() -> validator.validate("data-1", "req-1", "ts=123,v1=abc")).doesNotThrowAnyException();
    }

    @Test
    void validate_nullSecret_shouldSkipAndNotThrow() {
        ReflectionTestUtils.setField(validator, "webhookSecret", null);
        assertThatCode(() -> validator.validate("data-1", "req-1", "ts=123,v1=abc")).doesNotThrowAnyException();
    }

    @Test
    void validate_nullSignatureHeader_shouldThrow() {
        assertThatThrownBy(() -> validator.validate("data-1", "req-1", null))
                .isInstanceOf(WebhookAuthenticationException.class)
                .hasMessageContaining("Missing x-signature");
    }

    @Test
    void validate_blankSignatureHeader_shouldThrow() {
        assertThatThrownBy(() -> validator.validate("data-1", "req-1", ""))
                .isInstanceOf(WebhookAuthenticationException.class);
    }

    @Test
    void validate_missingTsField_shouldThrow() {
        assertThatThrownBy(() -> validator.validate("data-1", "req-1", "v1=abc123"))
                .isInstanceOf(WebhookAuthenticationException.class)
                .hasMessageContaining("Malformed");
    }

    @Test
    void validate_missingV1Field_shouldThrow() {
        assertThatThrownBy(() -> validator.validate("data-1", "req-1", "ts=123"))
                .isInstanceOf(WebhookAuthenticationException.class)
                .hasMessageContaining("Malformed");
    }

    @Test
    void validate_invalidTimestamp_shouldThrow() {
        assertThatThrownBy(() -> validator.validate("data-1", "req-1", "ts=not-a-number,v1=abc"))
                .isInstanceOf(WebhookAuthenticationException.class)
                .hasMessageContaining("Invalid timestamp");
    }

    @Test
    void validate_expiredTimestamp_shouldThrow() {
        long old = Instant.now().getEpochSecond() - 600;
        assertThatThrownBy(() -> validator.validate("data-1", "req-1", "ts=" + old + ",v1=abc"))
                .isInstanceOf(WebhookAuthenticationException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void validate_wrongSignature_shouldThrow() {
        long ts = Instant.now().getEpochSecond();
        assertThatThrownBy(() -> validator.validate("data-1", "req-1", "ts=" + ts + ",v1=wrongsignature"))
                .isInstanceOf(WebhookAuthenticationException.class)
                .hasMessageContaining("Invalid MercadoPago");
    }
}
