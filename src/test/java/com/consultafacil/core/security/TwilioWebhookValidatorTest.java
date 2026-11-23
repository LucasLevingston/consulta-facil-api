package com.consultafacil.core.security;

import com.consultafacil.core.config.TwilioProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TwilioWebhookValidatorTest {

    TwilioWebhookValidator validator;
    TwilioProperties properties;

    static final String AUTH_TOKEN = "test-auth-token-32chars-padded";
    static final String URL = "https://api.example.com/v1/webhook/whatsapp";

    @BeforeEach
    void setUp() {
        properties = new TwilioProperties();
        properties.setAuthToken(AUTH_TOKEN);
        validator = new TwilioWebhookValidator(properties);
    }

    private String buildSignature(String url, Map<String, String> params) throws Exception {
        StringBuilder manifest = new StringBuilder(url);
        new TreeMap<>(params).forEach((k, v) -> manifest.append(k).append(v));
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(AUTH_TOKEN.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        byte[] hash = mac.doFinal(manifest.toString().getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    @Test
    void validate_validSignature_shouldNotThrow() throws Exception {
        Map<String, String> params = Map.of("From", "whatsapp:+5511999990000", "Body", "Olá");
        String sig = buildSignature(URL, params);
        assertThatCode(() -> validator.validate(URL, params, sig)).doesNotThrowAnyException();
    }

    @Test
    void validate_validSignature_emptyParams_shouldNotThrow() throws Exception {
        String sig = buildSignature(URL, Map.of());
        assertThatCode(() -> validator.validate(URL, Map.of(), sig)).doesNotThrowAnyException();
    }

    @Test
    void validate_blankAuthToken_shouldSkipAndNotThrow() {
        properties.setAuthToken("");
        assertThatCode(() -> validator.validate(URL, Map.of(), "any-sig")).doesNotThrowAnyException();
    }

    @Test
    void validate_nullAuthToken_shouldSkipAndNotThrow() {
        properties.setAuthToken(null);
        assertThatCode(() -> validator.validate(URL, Map.of(), "any-sig")).doesNotThrowAnyException();
    }

    @Test
    void validate_nullSignature_shouldThrow() {
        assertThatThrownBy(() -> validator.validate(URL, Map.of(), null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Missing");
    }

    @Test
    void validate_blankSignature_shouldThrow() {
        assertThatThrownBy(() -> validator.validate(URL, Map.of(), ""))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Missing");
    }

    @Test
    void validate_wrongSignature_shouldThrow() {
        assertThatThrownBy(() -> validator.validate(URL, Map.of("Body", "hello"), "invalidsignature=="))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Invalid");
    }

    @Test
    void validate_paramOrderDoesNotMatter() throws Exception {
        Map<String, String> params = Map.of("From", "whatsapp:+5511", "Body", "hi", "To", "whatsapp:+14155");
        String sig = buildSignature(URL, params);
        // Pass params in different insertion order — TreeMap in validator normalizes order
        Map<String, String> reordered = Map.of("To", "whatsapp:+14155", "Body", "hi", "From", "whatsapp:+5511");
        assertThatCode(() -> validator.validate(URL, reordered, sig)).doesNotThrowAnyException();
    }
}
