package com.consultafacil.application.service.commission;
import com.consultafacil.application.service.referral.ReferralCommissionMapper;

import com.consultafacil.api.dto.billing.commission.ReferralCommissionDTO;
import com.consultafacil.application.port.in.wallet.AddPendingWalletCommissionUseCase;
import com.consultafacil.application.port.in.commission.CreateCommissionUseCase;
import com.consultafacil.domain.entity.ReferralCommission;
import com.consultafacil.domain.port.out.billing.BillingPaymentRepositoryPort;
import com.consultafacil.domain.port.out.referral.ReferralCommissionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateCommissionService implements CreateCommissionUseCase {

    private final ReferralCommissionRepositoryPort commissionRepository;
    private final BillingPaymentRepositoryPort billingPaymentRepository;
    private final AddPendingWalletCommissionUseCase addPendingWalletCommissionUseCase;
    private final ReferralCommissionMapper mapper;

    @Override
    @Transactional
    public ReferralCommissionDTO execute(String referralId, String paymentId, BigDecimal amount, String referrerId) {
        if (commissionRepository.existsByReferralIdAndPaymentId(referralId, paymentId)) {
            return commissionRepository.findByPaymentId(paymentId)
                    .map(mapper::toDTO)
                    .orElse(null);
        }

        BigDecimal commissionAmount = amount
                .multiply(BigDecimal.TEN)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        LocalDateTime availableAt = billingPaymentRepository.findById(paymentId)
                .map(p -> p.getPaidAt() != null ? p.getPaidAt().plusDays(30) : LocalDateTime.now().plusDays(30))
                .orElse(LocalDateTime.now().plusDays(30));

        ReferralCommission commission = commissionRepository.save(ReferralCommission.builder()
                .referralId(referralId)
                .paymentId(paymentId)
                .amount(commissionAmount)
                .availableAt(availableAt)
                .build());

        addPendingWalletCommissionUseCase.execute(referrerId, commissionAmount, commission.getId());
        return mapper.toDTO(commission);
    }
}
