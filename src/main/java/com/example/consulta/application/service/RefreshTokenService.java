package com.example.consulta.application.service;

import com.example.consulta.api.dto.auth.LoginResponseDTO;
import com.example.consulta.core.exception.UnauthorizedException;
import com.example.consulta.core.security.JwtTokenProvider;
import com.example.consulta.domain.entity.RefreshToken;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.port.out.RefreshTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int REFRESH_TOKEN_TTL_DAYS = 30;

    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public RefreshToken createFor(User user) {
        String rawToken = UUID.randomUUID().toString().replace("-", "");
        return refreshTokenRepository.save(RefreshToken.builder()
                .token(rawToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_TTL_DAYS))
                .build());
    }

    @Transactional
    public LoginResponseDTO rotate(String tokenValue) {
        RefreshToken existing = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (existing.isRevoked()) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }
        if (existing.isExpired()) {
            throw new UnauthorizedException("Refresh token has expired");
        }

        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        User user = existing.getUser();
        String newJwt = jwtTokenProvider.generateToken(user);
        RefreshToken newRefresh = createFor(user);

        log.info("[Auth] Refresh token rotated for userId={}", user.getId());
        return LoginResponseDTO.of(newJwt, newRefresh.getToken(),
                jwtTokenProvider.getExpiresIn(), user.getId(), user.getEmail(), user.getRole());
    }
}
