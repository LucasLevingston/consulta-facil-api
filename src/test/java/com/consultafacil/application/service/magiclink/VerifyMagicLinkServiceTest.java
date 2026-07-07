package com.consultafacil.application.service.magiclink;

import com.consultafacil.application.service.auth.CreateRefreshTokenService;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.JwtTokenProvider;
import com.consultafacil.domain.entity.MagicLinkToken;
import com.consultafacil.domain.entity.RefreshToken;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.MagicLinkTokenRepositoryPort;
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
    @Mock CreateRefreshTokenService createRefreshTokenService;

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
        when(createRefreshTokenService.createFor(any())).thenReturn(
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
                .hasMessageContaining("already been used");
    }

    @Test
    void execute_expiredToken_throwsBadRequest() {
        validToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByToken("valid-magic")).thenReturn(Optional.of(validToken));

        assertThatThrownBy(() -> service.execute("valid-magic"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }
}
