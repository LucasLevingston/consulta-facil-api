package com.consultafacil.application.service;

import com.consultafacil.api.dto.auth.LoginResponseDTO;
import com.consultafacil.core.exception.UnauthorizedException;
import com.consultafacil.core.security.JwtTokenProvider;
import com.consultafacil.domain.entity.RefreshToken;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.RefreshTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RotateRefreshTokenService {

    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CreateRefreshTokenService createRefreshTokenService;

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
        RefreshToken newRefresh = createRefreshTokenService.createFor(user);

        log.info("[Auth] Refresh token rotated for userId={}", user.getId());
        return LoginResponseDTO.of(newJwt, newRefresh.getToken(),
                jwtTokenProvider.getExpiresIn(), user.getId(), user.getEmail(), user.getRole());
    }
}
