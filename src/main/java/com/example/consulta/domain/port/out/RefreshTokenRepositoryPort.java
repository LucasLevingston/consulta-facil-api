package com.example.consulta.domain.port.out;

import com.example.consulta.domain.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepositoryPort {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByToken(String token);

    void deleteByUserId(String userId);
}
