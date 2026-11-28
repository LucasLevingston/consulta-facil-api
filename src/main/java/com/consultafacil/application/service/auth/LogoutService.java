package com.consultafacil.application.service.auth;

import com.consultafacil.application.port.in.auth.LogoutUseCase;
import com.consultafacil.domain.port.out.auth.JwtBlacklistPort;
import com.consultafacil.core.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final JwtBlacklistPort jwtBlacklistPort;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void execute(String bearerToken) {
        String token = stripBearer(bearerToken);
        long ttlSeconds = computeTtl(token);
        jwtBlacklistPort.blacklist(token, ttlSeconds);
        log.debug("User logged out, token blacklisted for {}s", ttlSeconds);
    }

    private String stripBearer(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }

    private long computeTtl(String token) {
        return jwtTokenProvider.getExpiration(token)
                .map(exp -> {
                    long remaining = (exp.getTime() - System.currentTimeMillis()) / 1000L;
                    return Math.max(remaining, 1L);
                })
                .orElse(1L);
    }
}
