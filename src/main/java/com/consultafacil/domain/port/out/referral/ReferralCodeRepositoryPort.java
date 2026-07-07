package com.consultafacil.domain.port.out.referral;

import com.consultafacil.domain.entity.ReferralCode;

import java.util.Optional;

public interface ReferralCodeRepositoryPort {
    ReferralCode save(ReferralCode referralCode);
    Optional<ReferralCode> findByUserId(String userId);
    Optional<ReferralCode> findByCode(String code);
    boolean existsByCode(String code);
}
