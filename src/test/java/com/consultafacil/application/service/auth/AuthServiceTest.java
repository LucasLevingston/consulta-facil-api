package com.consultafacil.application.service.auth;

import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.core.exception.UnauthorizedException;
import com.consultafacil.core.security.JwtTokenProvider;
import com.consultafacil.domain.entity.RefreshToken;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock UserRepositoryPort userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock CreateRefreshTokenService createRefreshTokenService;

    @InjectMocks AuthService service;

    User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "maxAttempts", 5);
        ReflectionTestUtils.setField(service, "lockoutDurationMinutes", 15);

        user = User.builder().id("u-1").name("João").email("j@e.com")
                .password("hashed-pass").role(UserRole.PATIENT).build();

        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtTokenProvider.generateToken(any())).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpiresIn()).thenReturn(86400L);
        when(createRefreshTokenService.createFor(any())).thenReturn(
                RefreshToken.builder().token("refresh-token").build());
    }

    private LoginRequestDTO req(String email, String password) {
        return new LoginRequestDTO(email, password);
    }

    @Test
    void login_validCredentials_returnsToken() {
        when(userRepository.findByEmail("j@e.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass123", "hashed-pass")).thenReturn(true);

        var result = service.execute(req("j@e.com", "pass123"));

        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getUserId()).isEqualTo("u-1");
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        when(userRepository.findByEmail("j@e.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.execute(req("j@e.com", "wrong")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_unknownEmail_throwsUnauthorized() {
        when(userRepository.findByEmail("unknown@e.com")).thenReturn(Optional.empty());
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.execute(req("unknown@e.com", "pass")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void login_lockedAccount_throwsUnauthorized() {
        user.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        when(userRepository.findByEmail("j@e.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.execute(req("j@e.com", "pass")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("locked");
    }

    @Test
    void login_googleOnlyAccount_throwsUnauthorized() {
        user.setPassword(null);
        when(userRepository.findByEmail("j@e.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.execute(req("j@e.com", "pass")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Google");
    }

    @Test
    void login_wrongPassword_incrementsFailedAttempts() {
        when(userRepository.findByEmail("j@e.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        try { service.execute(req("j@e.com", "wrong")); } catch (Exception ignored) {}

        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
    }

    @Test
    void login_successAfterFailure_resetsAttempts() {
        user.setFailedLoginAttempts(2);
        when(userRepository.findByEmail("j@e.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass123", "hashed-pass")).thenReturn(true);

        service.execute(req("j@e.com", "pass123"));

        assertThat(user.getFailedLoginAttempts()).isEqualTo(0);
    }
}
