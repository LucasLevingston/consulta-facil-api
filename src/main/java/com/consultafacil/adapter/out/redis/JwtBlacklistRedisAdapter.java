package com.consultafacil.adapter.out.redis;

import com.consultafacil.domain.port.out.auth.JwtBlacklistPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtBlacklistRedisAdapter implements JwtBlacklistPort {

    private static final String KEY_PREFIX = "jwt:blacklist:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void blacklist(String token, long ttlSeconds) {
        long effectiveTtl = Math.max(ttlSeconds, 1L);
        String key = KEY_PREFIX + token;
        try {
            stringRedisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(effectiveTtl));
            log.debug("Token blacklisted with TTL {}s", effectiveTtl);
        } catch (Exception ex) {
            log.error("Failed to blacklist token in Redis: {}", ex.getMessage());
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        String key = KEY_PREFIX + token;
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        } catch (Exception ex) {
            log.warn("Redis unavailable during blacklist check, failing open: {}", ex.getMessage());
            return false;
        }
    }
}
