package com.consultafacil.application.service.commission;

import com.consultafacil.application.port.in.commission.CreateCommissionUseCase;
import com.consultafacil.application.port.in.commission.HandlePaymentPaidCommissionUseCase;
import com.consultafacil.domain.port.out.referral.ReferralRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class HandlePaymentPaidCommissionService implements HandlePaymentPaidCommissionUseCase {

    private final ReferralRepositoryPort referralRepository;
    private final CreateCommissionUseCase createCommissionUseCase;

    @Override
    @Transactional
    public void execute(String paymentId, BigDecimal amount, String payerId) {
        referralRepository.findByReferredId(payerId).ifPresent(referral -> {
            if (referral.getFirstPaymentId() == null) {
                referral.setFirstPaymentId(paymentId);
                referralRepository.save(referral);
                createCommissionUseCase.execute(referral.getId(), paymentId, amount, referral.getReferrerId());
            }
        });
    }
}
