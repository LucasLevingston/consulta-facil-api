package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.commission.ReferralCommissionDTO;
import com.consultafacil.application.port.in.CommissionUseCase;
import com.consultafacil.application.port.in.WalletUseCase;
import com.consultafacil.domain.entity.ReferralCommission;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.enums.CommissionStatus;
import com.consultafacil.domain.port.out.BillingPaymentRepositoryPort;
import com.consultafacil.domain.port.out.ReferralCommissionRepositoryPort;
import com.consultafacil.domain.port.out.ReferralRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionService implements CommissionUseCase {

    private final ReferralCommissionRepositoryPort commissionRepository;
    private final ReferralRepositoryPort referralRepository;
    private final BillingPaymentRepositoryPort billingPaymentRepository;
    private final WalletUseCase walletUseCase;

    @Override
    @Transactional
    public void onPaymentPaid(String paymentId, BigDecimal amount, String payerId) {
        referralRepository.findByReferredId(payerId).ifPresent(referral -> {
            if (referral.getFirstPaymentId() == null) {
                referral.setFirstPaymentId(paymentId);
                referralRepository.save(referral);
                createCommission(referral.getId(), paymentId, amount, referral.getReferrerId());
            }
        });
    }

    @Override
    @Transactional
    public ReferralCommissionDTO createCommission(String referralId, String paymentId, BigDecimal amount, String referrerId) {
        if (commissionRepository.existsByReferralIdAndPaymentId(referralId, paymentId)) {
            return commissionRepository.findByPaymentId(paymentId)
                    .map(this::toDTO)
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

        walletUseCase.addPendingCommission(referrerId, commissionAmount, commission.getId());
        return toDTO(commission);
    }

    @Override
    @Transactional
    public void cancelCommission(String paymentId) {
        commissionRepository.findByPaymentId(paymentId).ifPresent(commission -> {
            if (commission.getStatus() == CommissionStatus.CANCELED) return;

            if (commission.getStatus() == CommissionStatus.PENDING) {
                referralRepository.findById(commission.getReferralId()).ifPresent(referral ->
                        walletUseCase.releasePending(referral.getReferrerId(), commission.getAmount()));
            }

            commission.setStatus(CommissionStatus.CANCELED);
            commissionRepository.save(commission);
        });
    }

    @Override
    @Transactional
    public void processAvailableCommissions() {
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
                walletUseCase.creditFromCommission(referral.getReferrerId(), commission.getAmount(), commission.getId());
                walletUseCase.releasePending(referral.getReferrerId(), commission.getAmount());
            });
            processed++;
        }
        log.info("[CommissionService] Processadas {} comissões disponíveis", processed);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReferralCommissionDTO> getAllCommissions() {
        return commissionRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ReferralCommissionDTO toDTO(ReferralCommission c) {
        return ReferralCommissionDTO.builder()
                .id(c.getId())
                .referralId(c.getReferralId())
                .paymentId(c.getPaymentId())
                .amount(c.getAmount())
                .percentage(c.getPercentage())
                .availableAt(c.getAvailableAt())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
