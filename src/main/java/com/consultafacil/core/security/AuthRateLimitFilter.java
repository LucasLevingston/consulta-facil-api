package com.consultafacil.core.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final String KEY_PREFIX = "rate_limit:";
    private static final Duration LOGIN_PERIOD = Duration.ofMinutes(1);
    private static final Duration FORGOT_PERIOD = Duration.ofMinutes(15);

    private final BucketConfiguration loginConfig;
    private final BucketConfiguration forgotConfig;
    private final LettuceBasedProxyManager<String> loginProxyManager;
    private final LettuceBasedProxyManager<String> forgotProxyManager;

    public AuthRateLimitFilter(
            @Value("${auth.rate-limit.login.requests-per-minute:5}") int loginRpm,
            @Value("${auth.rate-limit.forgot-password.requests-per-15min:3}") int forgotRpm,
            LettuceConnectionFactory lettuceConnectionFactory) {

        this.loginConfig = BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(loginRpm)
                        .refillGreedy(loginRpm, LOGIN_PERIOD)
                        .build())
                .build();

        this.forgotConfig = BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(forgotRpm)
                        .refillGreedy(forgotRpm, FORGOT_PERIOD)
                        .build())
                .build();

        LettuceBasedProxyManager<String> loginPm = null;
        LettuceBasedProxyManager<String> forgotPm = null;
        try {
            RedisClient redisClient = (RedisClient) lettuceConnectionFactory.getNativeClient();
            var codec = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
            loginPm = LettuceBasedProxyManager
                    .builderFor(redisClient.connect(codec))
                    .withExpirationStrategy(ExpirationAfterWriteStrategy.fixedTimeToLive(LOGIN_PERIOD))
                    .build();
            forgotPm = LettuceBasedProxyManager
                    .builderFor(redisClient.connect(codec))
                    .withExpirationStrategy(ExpirationAfterWriteStrategy.fixedTimeToLive(FORGOT_PERIOD))
                    .build();
        } catch (Exception e) {
            log.warn("Rate limit Redis unavailable at startup, fail-open mode active: {}", e.getMessage());
        }
        this.loginProxyManager = loginPm;
        this.forgotProxyManager = forgotPm;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!"POST".equals(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        String rateLimitKey = null;
        LettuceBasedProxyManager<String> proxyManager = null;
        BucketConfiguration config = null;
        Duration retryAfterPeriod = null;

        if (path.endsWith("/auth/login")) {
            rateLimitKey = KEY_PREFIX + "login:" + ip;
            proxyManager = loginProxyManager;
            config = loginConfig;
            retryAfterPeriod = LOGIN_PERIOD;
        } else if (path.endsWith("/auth/forgot-password")) {
            rateLimitKey = KEY_PREFIX + "forgot:" + ip;
            proxyManager = forgotProxyManager;
            config = forgotConfig;
            retryAfterPeriod = FORGOT_PERIOD;
        }

        if (rateLimitKey == null) {
            chain.doFilter(request, response);
            return;
        }

        if (proxyManager == null) {
            log.warn("Rate limit unavailable (fail open): ip={} path={}", ip, path);
            chain.doFilter(request, response);
            return;
        }

        try {
            final BucketConfiguration finalConfig = config;
            BucketProxy bucket = proxyManager.builder().build(rateLimitKey, () -> finalConfig);

            if (!bucket.tryConsume(1)) {
                log.warn("Rate limit exceeded: ip={} path={}", ip, path);
                writeTooManyRequests(response, retryAfterPeriod);
                return;
            }
        } catch (Exception e) {
            // Fail open: Redis unavailable should not block the API
            log.warn("Rate limit Redis error (fail open): ip={} path={}", ip, path, e);
        }

        chain.doFilter(request, response);
    }

    private void writeTooManyRequests(HttpServletResponse response, Duration retryAfter) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfter.toSeconds()));
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"Too many requests. Try again later.\",\"status\":429}");
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
