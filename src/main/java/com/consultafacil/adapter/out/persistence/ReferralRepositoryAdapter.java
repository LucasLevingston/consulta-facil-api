package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.Referral;
import com.consultafacil.domain.port.out.ReferralRepositoryPort;
import com.consultafacil.domain.repository.ReferralRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReferralRepositoryAdapter implements ReferralRepositoryPort {

    private final ReferralRepository referralRepository;

    @Override
    public Referral save(Referral referral) {
        return referralRepository.save(referral);
    }

    @Override
    public Optional<Referral> findById(String id) {
        return referralRepository.findById(id);
    }

    @Override
    public Optional<Referral> findByReferredId(String referredId) {
        return referralRepository.findByReferredId(referredId);
    }

    @Override
    public List<Referral> findAllByReferrerId(String referrerId) {
        return referralRepository.findAllByReferrerId(referrerId);
    }

    @Override
    public List<Referral> findAll() {
        return referralRepository.findAll();
    }
}
