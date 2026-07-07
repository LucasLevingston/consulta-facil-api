package com.consultafacil.application.service.auth;

import com.consultafacil.core.exception.UnauthorizedException;
import com.consultafacil.core.security.JwtTokenProvider;
import com.consultafacil.domain.entity.RefreshToken;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.auth.RefreshTokenRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RotateRefreshTokenServiceTest {

    @Mock RefreshTokenRepositoryPort refreshTokenRepository;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock CreateRefreshTokenService createRefreshTokenService;

    @InjectMocks RotateRefreshTokenService service;

    User user;
    RefreshToken validToken;

    @BeforeEach
    void setUp() {
        user = User.builder().id("u-1").email("u@e.com").name("User")
                .password("x").role(UserRole.PATIENT).build();

        validToken = RefreshToken.builder()
                .id("rt-1")
                .token("valid-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .revoked(false)
                .build();

        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtTokenProvider.generateToken(any())).thenReturn("new-jwt");
        when(jwtTokenProvider.getExpiresIn()).thenReturn(86400L);
        when(createRefreshTokenService.createFor(any())).thenReturn(
                RefreshToken.builder().token("new-refresh").user(user).build());
    }

    @Test
    void rotate_validToken_returnsNewTokens() {
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));

        var result = service.execute("valid-token");

        assertThat(result.getToken()).isEqualTo("new-jwt");
        assertThat(result.getRefreshToken()).isNotBlank();
        assertThat(validToken.isRevoked()).isTrue();
    }

    @Test
    void rotate_invalidToken_throwsUnauthorized() {
        when(refreshTokenRepository.findByToken("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("bad"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void rotate_revokedToken_throwsUnauthorized() {
        validToken.setRevoked(true);
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));

        assertThatThrownBy(() -> service.execute("valid-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("revoked");
    }

    @Test
    void rotate_expiredToken_throwsUnauthorized() {
        validToken.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));

        assertThatThrownBy(() -> service.execute("valid-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expired");
    }
}
