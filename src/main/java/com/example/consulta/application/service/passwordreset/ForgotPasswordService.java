package com.example.consulta.application.service.passwordreset;

import com.example.consulta.application.service.EmailService;
import com.example.consulta.domain.entity.PasswordResetToken;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.repository.PasswordResetTokenRepository;
import com.example.consulta.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${app.url:http://localhost:3000}")
    private String appUrl;

    @Transactional
    public void requestReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't reveal whether email exists
            log.debug("[PasswordReset] Reset requested for unknown email: {}", email);
            return;
        }

        User user = userOpt.get();
        tokenRepository.deleteByUserId(user.getId());

        String rawToken = UUID.randomUUID().toString();
        tokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .token(rawToken)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build());

        String resetUrl = appUrl + "/reset-password?token=" + rawToken;
        emailService.sendPasswordReset(user.getEmail(), user.getName(), resetUrl);
        log.info("[PasswordReset] Token issued for user {}", user.getId());
    }
}
