package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.ReferralCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReferralCodeRepository extends JpaRepository<ReferralCode, String> {
    Optional<ReferralCode> findByUserId(String userId);
    Optional<ReferralCode> findByCode(String code);
    boolean existsByCode(String code);
}
