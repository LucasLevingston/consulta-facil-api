package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.PasswordResetToken;
import com.consultafacil.domain.port.out.PasswordResetTokenRepositoryPort;
import com.consultafacil.domain.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepositoryPort {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        return passwordResetTokenRepository.save(token);
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    @Override
    public void deleteByUserId(String userId) {
        passwordResetTokenRepository.deleteByUserId(userId);
    }
}
