package com.consultafacil.core.security;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthRateLimitFilterTest {

    @Mock
    LettuceConnectionFactory lettuceConnectionFactory;

    @Mock
    FilterChain chain;

    @Mock
    LettuceBasedProxyManager<String> loginProxyManager;

    @Mock
    LettuceBasedProxyManager<String> forgotProxyManager;

    @Mock
    BucketProxy loginBucket;

    @Mock
    BucketProxy forgotBucket;

    AuthRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        // The constructor catches Exception from getNativeClient() — trigger fail-open path.
        when(lettuceConnectionFactory.getNativeClient())
                .thenThrow(new RuntimeException("no redis in unit tests"));

        filter = new AuthRateLimitFilter(5, 3, lettuceConnectionFactory);

        // Inject mock proxy managers via reflection so we can test the rate-limit path.
        ReflectionTestUtils.setField(filter, "loginProxyManager", loginProxyManager);
        ReflectionTestUtils.setField(filter, "forgotProxyManager", forgotProxyManager);
    }

    // ── Non-POST requests bypass the filter entirely ───────────────────────

    @Test
    void getRequest_passesThrough_noRateLimit() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v1/auth/login");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
        assertThat(res.getStatus()).isEqualTo(200);
    }

    @Test
    void putRequest_passesThrough_noRateLimit() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("PUT", "/v1/auth/login");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
    }

    // ── POST to non-rate-limited paths bypass the filter ─────────────────

    @Test
    void postToUnrelatedPath_passesThrough() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/v1/appointments");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
    }

    // ── Fail-open: proxy manager null means pass through ─────────────────

    @Test
    void failOpen_loginPath_proxyManagerNull_passesThrough() throws Exception {
        // Reset proxy managers to null to simulate Redis startup failure.
        ReflectionTestUtils.setField(filter, "loginProxyManager", null);
        ReflectionTestUtils.setField(filter, "forgotProxyManager", null);

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/v1/auth/login");
        req.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
        assertThat(res.getStatus()).isEqualTo(200);
    }

    @Test
    void failOpen_forgotPasswordPath_proxyManagerNull_passesThrough() throws Exception {
        ReflectionTestUtils.setField(filter, "loginProxyManager", null);
        ReflectionTestUtils.setField(filter, "forgotProxyManager", null);

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/v1/auth/forgot-password");
        req.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
    }

    // ── Token consumed: request allowed ──────────────────────────────────

    @Test
    void loginPath_tokenAvailable_allowsRequest() throws Exception {
        when(loginProxyManager.builder()).thenReturn(mock(io.github.bucket4j.distributed.proxy.RemoteBucketBuilder.class));
        io.github.bucket4j.distributed.proxy.RemoteBucketBuilder<String> builder =
                mock(io.github.bucket4j.distributed.proxy.RemoteBucketBuilder.class);
        when(loginProxyManager.builder()).thenReturn(builder);
        when(builder.build(any(String.class), any(Supplier.class))).thenReturn(loginBucket);
        when(loginBucket.tryConsume(1)).thenReturn(true);

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/v1/auth/login");
        req.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
        assertThat(res.getStatus()).isEqualTo(200);
    }

    @Test
    void forgotPasswordPath_tokenAvailable_allowsRequest() throws Exception {
        io.github.bucket4j.distributed.proxy.RemoteBucketBuilder<String> builder =
                mock(io.github.bucket4j.distributed.proxy.RemoteBucketBuilder.class);
        when(forgotProxyManager.builder()).thenReturn(builder);
        when(builder.build(any(String.class), any(Supplier.class))).thenReturn(forgotBucket);
        when(forgotBucket.tryConsume(1)).thenReturn(true);

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/v1/auth/forgot-password");
        req.setRemoteAddr("10.0.0.2");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
        assertThat(res.getStatus()).isEqualTo(200);
    }

    // ── Token exhausted: 429 response ────────────────────────────────────

    @Test
    void loginPath_tokenExhausted_returns429() throws Exception {
        io.github.bucket4j.distributed.proxy.RemoteBucketBuilder<String> builder =
                mock(io.github.bucket4j.distributed.proxy.RemoteBucketBuilder.class);
        when(loginProxyManager.builder()).thenReturn(builder);
        when(builder.build(any(String.class), any(Supplier.class))).thenReturn(loginBucket);
        when(loginBucket.tryConsume(1)).thenReturn(false);

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/v1/auth/login");
        req.setRemoteAddr("10.0.0.3");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, chain);

        verify(chain, never()).doFilter(any(), any());
        assertThat(res.getStatus()).isEqualTo(429);
        assertThat(res.getHeader("Retry-After")).isEqualTo("60");
        assertThat(res.getContentAsString()).contains("Too many requests");
    }

    @Test
    void forgotPasswordPath_tokenExhausted_returns429WithLongerRetryAfter() throws Exception {
        io.github.bucket4j.distributed.proxy.RemoteBucketBuilder<String> builder =
                mock(io.github.bucket4j.distributed.proxy.RemoteBucketBuilder.class);
        when(forgotProxyManager.builder()).thenReturn(builder);
        when(builder.build(any(String.class), any(Supplier.class))).thenReturn(forgotBucket);
        when(forgotBucket.tryConsume(1)).thenReturn(false);

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/v1/auth/forgot-password");
        req.setRemoteAddr("10.0.0.4");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, chain);

        verify(chain, never()).doFilter(any(), any());
        assertThat(res.getStatus()).isEqualTo(429);
        assertThat(res.getHeader("Retry-After")).isEqualTo("900");
    }

    // ── Redis exception during tryConsume: fail-open ───────────────────────

    @Test
    void loginPath_redisExceptionDuringConsume_failsOpen() throws Exception {
        io.github.bucket4j.distributed.proxy.RemoteBucketBuilder<String> builder =
                mock(io.github.bucket4j.distributed.proxy.RemoteBucketBuilder.class);
        when(loginProxyManager.builder()).thenReturn(builder);
        when(builder.build(any(String.class), any(Supplier.class))).thenReturn(loginBucket);
        when(loginBucket.tryConsume(1)).thenThrow(new RuntimeException("Redis connection lost"));

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/v1/auth/login");
        req.setRemoteAddr("10.0.0.5");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, chain);

        // Fail-open: request should pass through even on Redis error
        verify(chain).doFilter(req, res);
    }

    // ── IP extraction: X-Forwarded-For header ─────────────────────────────

    @Test
    void loginPath_xForwardedForHeader_usesFirstIp() throws Exception {
        io.github.bucket4j.distributed.proxy.RemoteBucketBuilder<String> builder =
                mock(io.github.bucket4j.distributed.proxy.RemoteBucketBuilder.class);
        when(loginProxyManager.builder()).thenReturn(builder);
        when(builder.build(any(String.class), any(Supplier.class))).thenReturn(loginBucket);
        when(loginBucket.tryConsume(1)).thenReturn(true);

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/v1/auth/login");
        req.addHeader("X-Forwarded-For", "192.168.1.1, 10.0.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
        // Verify the key used contains the first IP from the forwarded header.
        verify(builder).build(eq("rate_limit:login:192.168.1.1"), any(Supplier.class));
    }

    @Test
    void loginPath_noXForwardedFor_usesRemoteAddr() throws Exception {
        io.github.bucket4j.distributed.proxy.RemoteBucketBuilder<String> builder =
                mock(io.github.bucket4j.distributed.proxy.RemoteBucketBuilder.class);
        when(loginProxyManager.builder()).thenReturn(builder);
        when(builder.build(any(String.class), any(Supplier.class))).thenReturn(loginBucket);
        when(loginBucket.tryConsume(1)).thenReturn(true);

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/v1/auth/login");
        req.setRemoteAddr("172.16.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, chain);

        verify(builder).build(eq("rate_limit:login:172.16.0.1"), any(Supplier.class));
    }
}
