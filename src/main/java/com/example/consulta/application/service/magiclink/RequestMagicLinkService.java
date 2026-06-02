package com.example.consulta.application.service.magiclink;

import com.example.consulta.application.port.in.RequestMagicLinkUseCase;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.domain.entity.MagicLinkToken;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.port.out.EmailPort;
import com.example.consulta.domain.port.out.MagicLinkTokenRepositoryPort;
import com.example.consulta.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestMagicLinkService implements RequestMagicLinkUseCase {

    private static final int MAX_REQUESTS_PER_HOUR = 3;
    private static final int TTL_MINUTES = 15;

    private final UserRepositoryPort userRepository;
    private final MagicLinkTokenRepositoryPort tokenRepository;
    private final EmailPort emailPort;

    @Value("${app.url:http://localhost:3000}")
    private String appUrl;

    @Override
    @Transactional
    public void execute(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // don't reveal whether email exists
            log.debug("[MagicLink] Requested for unknown email: {}", email);
            return;
        }

        User user = userOpt.get();

        long recentCount = tokenRepository.countByUserIdAndCreatedAtAfter(
                user.getId(), LocalDateTime.now().minusHours(1));
        if (recentCount >= MAX_REQUESTS_PER_HOUR) {
            throw new BadRequestException("Muitas tentativas. Aguarde antes de solicitar um novo link.");
        }

        tokenRepository.deleteByUserId(user.getId());

        String rawToken = UUID.randomUUID().toString().replace("-", "");
        tokenRepository.save(MagicLinkToken.builder()
                .user(user)
                .token(rawToken)
                .expiresAt(LocalDateTime.now().plusMinutes(TTL_MINUTES))
                .build());

        String magicUrl = appUrl + "/auth/magic-link/verify?token=" + rawToken;
        emailPort.sendMagicLink(user.getEmail(), user.getName(), magicUrl);
        log.info("[MagicLink] Token issued for user {}", user.getId());
    }
}
