package com.consultafacil.application.service.passwordreset;

import com.consultafacil.domain.entity.PasswordResetToken;
import com.consultafacil.domain.port.out.PasswordResetTokenRepositoryPort;
import com.consultafacil.application.port.in.ResetPasswordUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPasswordUseCase {

    private final PasswordResetTokenRepositoryPort tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void execute(String rawToken, String newPassword) { reset(rawToken, newPassword); }

    @Transactional
    public void reset(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reset token"));

        if (token.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token has already been used");
        }
        if (token.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset token has expired");
        }

        token.getUser().setPassword(passwordEncoder.encode(newPassword));
        token.setUsed(true);
        tokenRepository.save(token);
        log.info("[PasswordReset] Password reset for user {}", token.getUser().getId());
    }
}
