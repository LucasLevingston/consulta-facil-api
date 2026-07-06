package com.consultafacil.application.service.magiclink;

import com.consultafacil.api.dto.auth.LoginResponseDTO;
import com.consultafacil.application.port.in.VerifyMagicLinkUseCase;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.JwtTokenProvider;
import com.consultafacil.domain.entity.MagicLinkToken;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.MagicLinkTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyMagicLinkService implements VerifyMagicLinkUseCase {

    private final MagicLinkTokenRepositoryPort tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.consultafacil.application.service.CreateRefreshTokenService createRefreshTokenService;

    @Override
    @Transactional
    public LoginResponseDTO execute(String token) {
        MagicLinkToken magicToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired magic link token"));

        if (magicToken.isUsed()) {
            throw new BadRequestException("This magic link has already been used.");
        }
        if (magicToken.isExpired()) {
            throw new BadRequestException("This magic link has expired. Please request a new one.");
        }

        magicToken.setUsed(true);
        tokenRepository.save(magicToken);

        User user = magicToken.getUser();
        String jwt = jwtTokenProvider.generateToken(user);
        String refreshToken = createRefreshTokenService.createFor(user).getToken();
        log.info("[MagicLink] Login via magic link userId={}", user.getId());

        return LoginResponseDTO.of(jwt, refreshToken, jwtTokenProvider.getExpiresIn(),
                user.getId(), user.getEmail(), user.getRole());
    }
}
