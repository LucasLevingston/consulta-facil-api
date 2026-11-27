package com.consultafacil.api.security;

import com.consultafacil.ConsultaFacilApplication;
import com.consultafacil.domain.port.out.EmailPort;
import com.consultafacil.domain.port.out.StoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Rate-limiting integration test using a real embedded server (RANDOM_PORT) so that the full
 * Servlet filter chain — including AuthRateLimitFilter — is exercised end-to-end.
 *
 * <p>Uses {@code auth.rate-limit.login.requests-per-minute=2} to keep the test fast
 * (3 requests trigger 429) while still verifying the per-IP bucket logic.
 */
@SpringBootTest(
        classes = ConsultaFacilApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestPropertySource(properties = {
        "spring.profiles.active=test",
        "auth.rate-limit.login.requests-per-minute=2"
})
class RateLimitIntegrationTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private StoragePort storagePort;

    @MockitoBean
    private EmailPort emailPort;

    private RestTemplate restTemplate;
    private String loginUrl;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        loginUrl = "http://localhost:" + port + "/auth/login";
    }

    // =========================================================================
    // Rate limiting: excessive login attempts from same IP return 429
    // =========================================================================

    @Test
    void rateLimitExcessiveLoginAttemptsReturn429() {
        // A unique IP derived from UUID ensures a fresh bucket every time this test
        // runs — even when re-run against the same embedded server within a session.
        String uniqueIp = "192.0.2." + ((Math.abs(UUID.randomUUID().getLeastSignificantBits()) % 254) + 1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Forwarded-For", uniqueIp);

        String body = "{\"email\":\"nobody@ratelimit.com\",\"password\":\"doesnotmatter\"}";
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        // With limit=2: requests 1-2 get 401 (wrong credentials), request 3 gets 429
        boolean got429 = false;
        int lastStatus = 0;
        for (int i = 0; i < 5; i++) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        loginUrl, HttpMethod.POST, request, String.class);
                lastStatus = response.getStatusCode().value();
            } catch (HttpClientErrorException ex) {
                lastStatus = ex.getStatusCode().value();
                if (lastStatus == 429) {
                    got429 = true;
                    break;
                }
            }
        }
        assert got429 : "Expected 429 after exceeding login rate limit; last status was " + lastStatus;
    }

    @Test
    void rateLimitDoesNotApplyToNonRateLimitedEndpoints() {
        // GET requests are excluded from rate-limiting in AuthRateLimitFilter
        String uniqueIp = "203.0.113."
                + ((Math.abs(UUID.randomUUID().getLeastSignificantBits()) % 254) + 1);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Forwarded-For", uniqueIp);

        String url = "http://localhost:" + port + "/professionals";
        HttpEntity<Void> request = new HttpEntity<>(headers);

        for (int i = 0; i < 5; i++) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        url, HttpMethod.GET, request, String.class);
                int status = response.getStatusCode().value();
                assert status != 429
                        : "GET /professionals should never be rate-limited (got 429 on attempt " + (i + 1) + ")";
            } catch (HttpClientErrorException ex) {
                assert ex.getStatusCode().value() != 429
                        : "GET /professionals should never be rate-limited (got 429 on attempt " + (i + 1) + ")";
            }
        }
    }
}
