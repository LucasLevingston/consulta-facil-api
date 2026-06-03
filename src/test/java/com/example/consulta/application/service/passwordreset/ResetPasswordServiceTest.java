package com.example.consulta.application.service.passwordreset;

import com.example.consulta.domain.entity.PasswordResetToken;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.port.out.PasswordResetTokenRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceTest {

    @Mock PasswordResetTokenRepositoryPort tokenRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks ResetPasswordService service;

    private User user;
    private PasswordResetToken validToken;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("user-1")
                .email("joao@email.com")
                .name("João Silva")
                .password("old-hashed")
                .role(UserRole.PATIENT)
                .build();

        validToken = PasswordResetToken.builder()
                .id("token-1")
                .user(user)
                .token("valid-raw-token")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        lenient().when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void reset_validToken_shouldEncodeAndSetNewPassword() {
        when(tokenRepository.findByToken("valid-raw-token")).thenReturn(Optional.of(validToken));
        when(passwordEncoder.encode("newPassword123")).thenReturn("new-hashed");

        service.reset("valid-raw-token", "newPassword123");

        assertThat(user.getPassword()).isEqualTo("new-hashed");
    }

    @Test
    void reset_validToken_shouldMarkTokenAsUsed() {
        when(tokenRepository.findByToken("valid-raw-token")).thenReturn(Optional.of(validToken));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        service.reset("valid-raw-token", "newPassword123");

        assertThat(validToken.isUsed()).isTrue();
        verify(tokenRepository).save(validToken);
    }

    @Test
    void reset_invalidToken_shouldThrow400() {
        when(tokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reset("bad-token", "newPassword"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Token inválido");
    }

    @Test
    void reset_alreadyUsedToken_shouldThrow400() {
        validToken.setUsed(true);
        when(tokenRepository.findByToken("valid-raw-token")).thenReturn(Optional.of(validToken));

        assertThatThrownBy(() -> service.reset("valid-raw-token", "newPassword"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Token já utilizado");
    }

    @Test
    void reset_expiredToken_shouldThrow400() {
        var expiredToken = PasswordResetToken.builder()
                .user(user)
                .token("expired-token")
                .expiresAt(LocalDateTime.now().minusHours(1))
                .used(false)
                .build();
        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> service.reset("expired-token", "newPassword"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Token expirado");
    }

    @Test
    void reset_validToken_shouldNotInteractWithEmailService() {
        when(tokenRepository.findByToken("valid-raw-token")).thenReturn(Optional.of(validToken));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        service.reset("valid-raw-token", "newPassword123");

        verify(tokenRepository, times(1)).save(any());
        verifyNoMoreInteractions(tokenRepository);
    }
}
