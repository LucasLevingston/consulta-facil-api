package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.Referral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReferralRepository extends JpaRepository<Referral, String> {
    Optional<Referral> findByReferrerId(String referrerId);
    Optional<Referral> findByReferredId(String referredId);
    List<Referral> findAllByReferrerId(String referrerId);
}
