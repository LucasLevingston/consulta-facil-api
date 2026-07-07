package com.consultafacil.adapter.out.persistence.referral;

import com.consultafacil.domain.entity.ReferralCode;
import com.consultafacil.domain.port.out.referral.ReferralCodeRepositoryPort;
import com.consultafacil.domain.repository.referral.ReferralCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReferralCodeRepositoryAdapter implements ReferralCodeRepositoryPort {

    private final ReferralCodeRepository referralCodeRepository;

    @Override
    public ReferralCode save(ReferralCode referralCode) {
        return referralCodeRepository.save(referralCode);
    }

    @Override
    public Optional<ReferralCode> findByUserId(String userId) {
        return referralCodeRepository.findByUserId(userId);
    }

    @Override
    public Optional<ReferralCode> findByCode(String code) {
        return referralCodeRepository.findByCode(code);
    }

    @Override
    public boolean existsByCode(String code) {
        return referralCodeRepository.existsByCode(code);
    }
}
