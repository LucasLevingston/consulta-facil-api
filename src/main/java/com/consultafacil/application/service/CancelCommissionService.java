package com.consultafacil.application.service;

import com.consultafacil.application.port.in.CancelCommissionUseCase;
import com.consultafacil.application.port.in.ReleasePendingWalletCommissionUseCase;
import com.consultafacil.domain.enums.CommissionStatus;
import com.consultafacil.domain.port.out.ReferralCommissionRepositoryPort;
import com.consultafacil.domain.port.out.ReferralRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelCommissionService implements CancelCommissionUseCase {

    private final ReferralCommissionRepositoryPort commissionRepository;
    private final ReferralRepositoryPort referralRepository;
    private final ReleasePendingWalletCommissionUseCase releasePendingWalletCommissionUseCase;

    @Override
    @Transactional
    public void execute(String paymentId) {
        commissionRepository.findByPaymentId(paymentId).ifPresent(commission -> {
            if (commission.getStatus() == CommissionStatus.CANCELED) return;

            if (commission.getStatus() == CommissionStatus.PENDING) {
                referralRepository.findById(commission.getReferralId()).ifPresent(referral ->
                        releasePendingWalletCommissionUseCase.execute(referral.getReferrerId(), commission.getAmount()));
            }

            commission.setStatus(CommissionStatus.CANCELED);
            commissionRepository.save(commission);
        });
    }
}
