package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.Referral;

import java.util.List;
import java.util.Optional;

public interface ReferralRepositoryPort {
    Referral save(Referral referral);
    Optional<Referral> findById(String id);
    Optional<Referral> findByReferredId(String referredId);
    List<Referral> findAllByReferrerId(String referrerId);
    List<Referral> findAll();
}
