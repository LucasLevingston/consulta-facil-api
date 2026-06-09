package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.RefreshToken;
import com.consultafacil.domain.port.out.RefreshTokenRepositoryPort;
import com.consultafacil.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public RefreshToken save(RefreshToken token) {
        return refreshTokenRepository.save(token);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public void deleteByUserId(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
