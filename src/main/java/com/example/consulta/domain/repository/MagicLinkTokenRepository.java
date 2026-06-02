package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.MagicLinkToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MagicLinkTokenRepository extends JpaRepository<MagicLinkToken, String> {
    Optional<MagicLinkToken> findByToken(String token);
    void deleteByUserId(String userId);
    long countByUserIdAndCreatedAtAfter(String userId, java.time.LocalDateTime since);
}
