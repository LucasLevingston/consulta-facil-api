package com.consultafacil.application.service.commission;

import com.consultafacil.application.port.in.wallet.CreditWalletFromCommissionUseCase;
import com.consultafacil.application.port.in.commission.ProcessAvailableCommissionsUseCase;
import com.consultafacil.application.port.in.wallet.ReleasePendingWalletCommissionUseCase;
import com.consultafacil.domain.entity.ReferralCommission;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.enums.CommissionStatus;
import com.consultafacil.domain.port.out.billing.BillingPaymentRepositoryPort;
import com.consultafacil.domain.port.out.referral.ReferralCommissionRepositoryPort;
import com.consultafacil.domain.port.out.referral.ReferralRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessAvailableCommissionsService implements ProcessAvailableCommissionsUseCase {

    private final ReferralCommissionRepositoryPort commissionRepository;
    private final ReferralRepositoryPort referralRepository;
    private final BillingPaymentRepositoryPort billingPaymentRepository;
    private final CreditWalletFromCommissionUseCase creditWalletFromCommissionUseCase;
    private final ReleasePendingWalletCommissionUseCase releasePendingWalletCommissionUseCase;

    @Override
    @Transactional
    public void execute() {
        LocalDateTime now = LocalDateTime.now();
        List<ReferralCommission> pending = commissionRepository
                .findByStatusAndAvailableAtBefore(CommissionStatus.PENDING, now);

        int processed = 0;
        for (ReferralCommission commission : pending) {
            boolean paymentValid = billingPaymentRepository.findById(commission.getPaymentId())
                    .map(p -> p.getStatus() != BillingPaymentStatus.REFUNDED
                            && p.getStatus() != BillingPaymentStatus.CANCELED)
                    .orElse(false);

            if (!paymentValid) continue;

            commission.setStatus(CommissionStatus.AVAILABLE);
            commissionRepository.save(commission);

            referralRepository.findById(commission.getReferralId()).ifPresent(referral -> {
                creditWalletFromCommissionUseCase.execute(referral.getReferrerId(), commission.getAmount(), commission.getId());
                releasePendingWalletCommissionUseCase.execute(referral.getReferrerId(), commission.getAmount());
            });
            processed++;
        }
        log.info("[ProcessAvailableCommissionsService] Processadas {} comissões disponíveis", processed);
    }
}
