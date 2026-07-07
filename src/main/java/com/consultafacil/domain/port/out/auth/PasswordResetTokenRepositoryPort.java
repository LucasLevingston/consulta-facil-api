package com.consultafacil.domain.port.out.auth;

import com.consultafacil.domain.entity.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepositoryPort {

    PasswordResetToken save(PasswordResetToken token);

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUserId(String userId);
}
