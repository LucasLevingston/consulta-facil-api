package com.consultafacil.application.service.passwordreset;

import com.consultafacil.domain.port.out.EmailPort;
import com.consultafacil.domain.entity.PasswordResetToken;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.PasswordResetTokenRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import com.consultafacil.application.port.in.ForgotPasswordUseCase;
import com.consultafacil.core.util.PiiMask;
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
public class ForgotPasswordService implements ForgotPasswordUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordResetTokenRepositoryPort tokenRepository;
    private final EmailPort emailService;

    @Value("${app.url:http://localhost:3000}")
    private String appUrl;

    @Override
    public void execute(String email) { requestReset(email); }

    @Transactional
    public void requestReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't reveal whether email exists
            log.debug("[PasswordReset] Reset requested for unknown email: {}", PiiMask.maskEmail(email));
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
