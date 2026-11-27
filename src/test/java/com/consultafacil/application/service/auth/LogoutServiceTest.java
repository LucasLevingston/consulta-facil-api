package com.consultafacil.application.service.auth;

import com.consultafacil.core.security.JwtTokenProvider;
import com.consultafacil.domain.port.out.auth.JwtBlacklistPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    JwtBlacklistPort jwtBlacklistPort;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    LogoutService logoutService;

    @Test
    void execute_validBearerToken_blacklistsTokenWithCorrectTtl() {
        String rawToken = "eyJhbGciOiJIUzUxMiJ9.valid.token";
        String bearerToken = "Bearer " + rawToken;
        long futureMs = System.currentTimeMillis() + 3600_000L;
        when(jwtTokenProvider.getExpiration(rawToken)).thenReturn(Optional.of(new Date(futureMs)));

        logoutService.execute(bearerToken);

        verify(jwtBlacklistPort).blacklist(eq(rawToken), longThat(ttl -> ttl > 3590L && ttl <= 3600L));
    }

    @Test
    void execute_tokenWithoutBearerPrefix_blacklistsAsIs() {
        String rawToken = "plain.token.value";
        long futureMs = System.currentTimeMillis() + 60_000L;
        when(jwtTokenProvider.getExpiration(rawToken)).thenReturn(Optional.of(new Date(futureMs)));

        logoutService.execute(rawToken);

        verify(jwtBlacklistPort).blacklist(eq(rawToken), longThat(ttl -> ttl >= 1L));
    }

    @Test
    void execute_expiredToken_blacklistsWithMinimumTtlOfOne() {
        String rawToken = "expired.token";
        long pastMs = System.currentTimeMillis() - 10_000L;
        when(jwtTokenProvider.getExpiration(rawToken)).thenReturn(Optional.of(new Date(pastMs)));

        logoutService.execute(rawToken);

        verify(jwtBlacklistPort).blacklist(eq(rawToken), eq(1L));
    }

    @Test
    void execute_unparseableToken_blacklistsWithFallbackTtl() {
        String rawToken = "unparseable";
        when(jwtTokenProvider.getExpiration(rawToken)).thenReturn(Optional.empty());

        logoutService.execute(rawToken);

        verify(jwtBlacklistPort).blacklist(eq(rawToken), eq(1L));
    }

    @Test
    void execute_nullToken_doesNotThrow() {
        when(jwtTokenProvider.getExpiration(null)).thenReturn(Optional.empty());

        logoutService.execute(null);

        verify(jwtBlacklistPort).blacklist(eq(null), eq(1L));
    }
}
