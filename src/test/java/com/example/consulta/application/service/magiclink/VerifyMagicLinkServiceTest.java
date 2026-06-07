package com.example.consulta.application.service.magiclink;

import com.example.consulta.application.service.RefreshTokenService;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.core.security.JwtTokenProvider;
import com.example.consulta.domain.entity.MagicLinkToken;
import com.example.consulta.domain.entity.RefreshToken;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.port.out.MagicLinkTokenRepositoryPort;
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
class VerifyMagicLinkServiceTest {

    @Mock MagicLinkTokenRepositoryPort tokenRepository;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock RefreshTokenService refreshTokenService;

    @InjectMocks VerifyMagicLinkService service;

    User user;
    MagicLinkToken validToken;

    @BeforeEach
    void setUp() {
        user = User.builder().id("u-1").email("u@e.com").name("User")
                .password("x").role(UserRole.PATIENT).build();

        validToken = MagicLinkToken.builder()
                .id("ml-1")
                .token("valid-magic")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        when(jwtTokenProvider.generateToken(any())).thenReturn("jwt");
        when(jwtTokenProvider.getExpiresIn()).thenReturn(86400L);
        when(refreshTokenService.createFor(any())).thenReturn(
                RefreshToken.builder().token("refresh").build());
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void execute_validToken_returnsJwt() {
        when(tokenRepository.findByToken("valid-magic")).thenReturn(Optional.of(validToken));

        var result = service.execute("valid-magic");

        assertThat(result.getToken()).isEqualTo("jwt");
        assertThat(result.getRefreshToken()).isEqualTo("refresh");
        assertThat(validToken.isUsed()).isTrue();
    }

    @Test
    void execute_invalidToken_throwsNotFound() {
        when(tokenRepository.findByToken("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("bad"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_alreadyUsed_throwsBadRequest() {
        validToken.setUsed(true);
        when(tokenRepository.findByToken("valid-magic")).thenReturn(Optional.of(validToken));

        assertThatThrownBy(() -> service.execute("valid-magic"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("utilizado");
    }

    @Test
    void execute_expiredToken_throwsBadRequest() {
        validToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByToken("valid-magic")).thenReturn(Optional.of(validToken));

        assertThatThrownBy(() -> service.execute("valid-magic"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expirou");
    }
}
