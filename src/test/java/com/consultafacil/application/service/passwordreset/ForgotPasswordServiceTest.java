package com.consultafacil.application.service.passwordreset;

import com.consultafacil.domain.entity.PasswordResetToken;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.EmailPort;
import com.consultafacil.domain.port.out.PasswordResetTokenRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordServiceTest {

    @Mock UserRepositoryPort userRepository;
    @Mock PasswordResetTokenRepositoryPort tokenRepository;
    @Mock EmailPort emailService;

    @InjectMocks ForgotPasswordService service;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "appUrl", "http://localhost:3000");
        user = User.builder()
                .id("user-1")
                .email("joao@email.com")
                .name("João Silva")
                .password("hashed")
                .role(UserRole.PATIENT)
                .build();
        lenient().when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void requestReset_existingEmail_shouldDeleteOldTokenAndSaveNew() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));

        service.requestReset("joao@email.com");

        verify(tokenRepository).deleteByUserId("user-1");
        verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void requestReset_existingEmail_shouldSendResetEmail() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));

        service.requestReset("joao@email.com");

        var captor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordReset(
                captor.capture(), eq("João Silva"), anyString());
        assertThat(captor.getValue()).isEqualTo("joao@email.com");
    }

    @Test
    void requestReset_existingEmail_resetUrlShouldContainToken() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));

        service.requestReset("joao@email.com");

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordReset(anyString(), anyString(), urlCaptor.capture());
        assertThat(urlCaptor.getValue()).startsWith("http://localhost:3000/reset-password?token=");
    }

    @Test
    void requestReset_savedTokenShouldExpireInOneHour() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));

        service.requestReset("joao@email.com");

        var captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue().getExpiresAt())
                .isAfterOrEqualTo(java.time.LocalDateTime.now().plusMinutes(59));
    }

    @Test
    void requestReset_unknownEmail_shouldNotSendEmail() {
        when(userRepository.findByEmail("unknown@email.com")).thenReturn(Optional.empty());

        service.requestReset("unknown@email.com");

        verifyNoInteractions(emailService);
        verifyNoInteractions(tokenRepository);
    }

    @Test
    void requestReset_unknownEmail_shouldNotThrow() {
        when(userRepository.findByEmail("unknown@email.com")).thenReturn(Optional.empty());

        service.requestReset("unknown@email.com");
    }
}
