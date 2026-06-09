package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepositoryPort {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByToken(String token);

    void deleteByUserId(String userId);
}
