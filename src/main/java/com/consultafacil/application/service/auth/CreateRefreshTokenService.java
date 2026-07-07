package com.consultafacil.application.service.auth;

import com.consultafacil.domain.entity.RefreshToken;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.RefreshTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateRefreshTokenService {

    private static final int REFRESH_TOKEN_TTL_DAYS = 30;

    private final RefreshTokenRepositoryPort refreshTokenRepository;

    public RefreshToken createFor(User user) {
        String rawToken = UUID.randomUUID().toString().replace("-", "");
        return refreshTokenRepository.save(RefreshToken.builder()
                .token(rawToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_TTL_DAYS))
                .build());
    }
}
