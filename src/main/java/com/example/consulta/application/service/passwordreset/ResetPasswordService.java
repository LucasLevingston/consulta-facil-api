package com.example.consulta.application.service.passwordreset;

import com.example.consulta.domain.entity.PasswordResetToken;
import com.example.consulta.domain.repository.PasswordResetTokenRepository;
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
public class ResetPasswordService {

    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void reset(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido"));

        if (token.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token já utilizado");
        }
        if (token.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expirado");
        }

        token.getUser().setPassword(passwordEncoder.encode(newPassword));
        token.setUsed(true);
        tokenRepository.save(token);
        log.info("[PasswordReset] Password reset for user {}", token.getUser().getId());
    }
}
