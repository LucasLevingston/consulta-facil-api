package com.consultafacil.application.service.referral;

import com.consultafacil.api.dto.billing.referral.ReferralCodeDTO;
import com.consultafacil.api.dto.billing.referral.ReferralStatsDTO;
import com.consultafacil.application.port.in.GetOrCreateReferralCodeUseCase;
import com.consultafacil.application.port.in.GetUserReferralStatsUseCase;
import com.consultafacil.domain.entity.Referral;
import com.consultafacil.domain.enums.CommissionStatus;
import com.consultafacil.domain.port.out.ReferralCommissionRepositoryPort;
import com.consultafacil.domain.port.out.ReferralRepositoryPort;
import com.consultafacil.domain.port.out.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetUserReferralStatsService implements GetUserReferralStatsUseCase {

    private final GetOrCreateReferralCodeUseCase getOrCreateReferralCodeUseCase;
    private final ReferralRepositoryPort referralRepository;
    private final ReferralCommissionRepositoryPort referralCommissionRepository;
    private final WalletRepositoryPort walletRepository;

    @Override
    @Transactional(readOnly = true)
    public ReferralStatsDTO execute(String userId) {
        ReferralCodeDTO codeDTO = getOrCreateReferralCodeUseCase.execute(userId);
        List<Referral> referrals = referralRepository.findAllByReferrerId(userId);

        List<String> referralIds = referrals.stream().map(Referral::getId).collect(Collectors.toList());
        long pendingCommissions = referralIds.isEmpty() ? 0 : referralCommissionRepository.findAll().stream()
                .filter(c -> referralIds.contains(c.getReferralId()) && c.getStatus() == CommissionStatus.PENDING)
                .count();
        long availableCommissions = referralIds.isEmpty() ? 0 : referralCommissionRepository.findAll().stream()
                .filter(c -> referralIds.contains(c.getReferralId()) && c.getStatus() == CommissionStatus.AVAILABLE)
                .count();

        BigDecimal pendingBalance = walletRepository.findByUserId(userId)
                .map(w -> w.getPendingBalance())
                .orElse(BigDecimal.ZERO);
        BigDecimal availableBalance = walletRepository.findByUserId(userId)
                .map(w -> w.getBalance())
                .orElse(BigDecimal.ZERO);

        return ReferralStatsDTO.builder()
                .code(codeDTO.getCode())
                .totalReferred(referrals.size())
                .pendingCommissions(pendingCommissions)
                .availableCommissions(availableCommissions)
                .pendingBalance(pendingBalance)
                .availableBalance(availableBalance)
                .build();
    }
}
