package com.consultafacil.adapter.out.persistence.referral;

import com.consultafacil.domain.entity.ReferralCommission;
import com.consultafacil.domain.enums.CommissionStatus;
import com.consultafacil.domain.port.out.referral.ReferralCommissionRepositoryPort;
import com.consultafacil.domain.repository.referral.ReferralCommissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReferralCommissionRepositoryAdapter implements ReferralCommissionRepositoryPort {

    private final ReferralCommissionRepository referralCommissionRepository;

    @Override
    public ReferralCommission save(ReferralCommission commission) {
        return referralCommissionRepository.save(commission);
    }

    @Override
    public Optional<ReferralCommission> findByPaymentId(String paymentId) {
        return referralCommissionRepository.findByPaymentId(paymentId);
    }

    @Override
    public List<ReferralCommission> findByStatus(CommissionStatus status) {
        return referralCommissionRepository.findByStatus(status);
    }

    @Override
    public List<ReferralCommission> findByStatusAndAvailableAtBefore(CommissionStatus status, LocalDateTime now) {
        return referralCommissionRepository.findByStatusAndAvailableAtBefore(status, now);
    }

    @Override
    public boolean existsByReferralIdAndPaymentId(String referralId, String paymentId) {
        return referralCommissionRepository.existsByReferralIdAndPaymentId(referralId, paymentId);
    }

    @Override
    public List<ReferralCommission> findAll() {
        return referralCommissionRepository.findAll();
    }
}
