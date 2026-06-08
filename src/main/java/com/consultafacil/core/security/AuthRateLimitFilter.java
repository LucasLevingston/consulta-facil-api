package com.consultafacil.core.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    @Value("${auth.rate-limit.login.requests-per-minute:5}")
    private int loginRequestsPerMinute;

    @Value("${auth.rate-limit.forgot-password.requests-per-15min:3}")
    private int forgotPasswordRequestsPer15Min;

    private final ConcurrentHashMap<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> forgotBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if (!"POST".equals(method)) {
            chain.doFilter(request, response);
            return;
        }

        Bucket bucket = null;
        String ip = getClientIp(request);

        if (path.endsWith("/auth/login")) {
            bucket = loginBuckets.computeIfAbsent(ip, k -> newLoginBucket());
        } else if (path.endsWith("/auth/forgot-password")) {
            bucket = forgotBuckets.computeIfAbsent(ip, k -> newForgotPasswordBucket());
        }

        if (bucket != null && !bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for IP={} path={}", ip, path);
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Too many requests. Try again later.\",\"status\":429}");
            return;
        }

        chain.doFilter(request, response);
    }

    private Bucket newLoginBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(loginRequestsPerMinute)
                        .refillGreedy(loginRequestsPerMinute, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    private Bucket newForgotPasswordBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(forgotPasswordRequestsPer15Min)
                        .refillGreedy(forgotPasswordRequestsPer15Min, Duration.ofMinutes(15))
                        .build())
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // Cleanup stale buckets hourly to prevent memory leak on long-running instances
    @Scheduled(fixedDelay = 3_600_000)
    public void cleanupStaleBuckets() {
        int beforeLogin = loginBuckets.size();
        int beforeForgot = forgotBuckets.size();
        loginBuckets.clear();
        forgotBuckets.clear();
        log.debug("Rate limit bucket cleanup: removed {} login + {} forgot-password buckets",
                beforeLogin, beforeForgot);
    }
}
