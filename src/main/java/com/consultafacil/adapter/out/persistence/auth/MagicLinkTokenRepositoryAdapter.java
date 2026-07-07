package com.consultafacil.adapter.out.persistence.auth;

import com.consultafacil.domain.entity.MagicLinkToken;
import com.consultafacil.domain.port.out.auth.MagicLinkTokenRepositoryPort;
import com.consultafacil.domain.repository.auth.MagicLinkTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MagicLinkTokenRepositoryAdapter implements MagicLinkTokenRepositoryPort {

    private final MagicLinkTokenRepository magicLinkTokenRepository;

    @Override
    public MagicLinkToken save(MagicLinkToken token) {
        return magicLinkTokenRepository.save(token);
    }

    @Override
    public Optional<MagicLinkToken> findByToken(String token) {
        return magicLinkTokenRepository.findByToken(token);
    }

    @Override
    public void deleteByUserId(String userId) {
        magicLinkTokenRepository.deleteByUserId(userId);
    }

    @Override
    public long countByUserIdAndCreatedAtAfter(String userId, LocalDateTime since) {
        return magicLinkTokenRepository.countByUserIdAndCreatedAtAfter(userId, since);
    }
}
