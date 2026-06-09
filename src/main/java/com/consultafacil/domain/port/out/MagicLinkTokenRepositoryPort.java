package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.MagicLinkToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MagicLinkTokenRepositoryPort {

    MagicLinkToken save(MagicLinkToken token);

    Optional<MagicLinkToken> findByToken(String token);

    void deleteByUserId(String userId);

    long countByUserIdAndCreatedAtAfter(String userId, LocalDateTime since);
}
