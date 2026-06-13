package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.referral.ReferralCodeDTO;
import com.consultafacil.api.dto.billing.referral.ReferralDTO;
import com.consultafacil.api.dto.billing.referral.ReferralStatsDTO;
import com.consultafacil.application.port.in.ReferralUseCase;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.DuplicateResourceException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Referral;
import com.consultafacil.domain.entity.ReferralCode;
import com.consultafacil.domain.enums.CommissionStatus;
import com.consultafacil.domain.port.out.ReferralCodeRepositoryPort;
import com.consultafacil.domain.port.out.ReferralCommissionRepositoryPort;
import com.consultafacil.domain.port.out.ReferralRepositoryPort;
import com.consultafacil.domain.port.out.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferralService implements ReferralUseCase {

    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ReferralCodeRepositoryPort referralCodeRepository;
    private final ReferralRepositoryPort referralRepository;
    private final ReferralCommissionRepositoryPort referralCommissionRepository;
    private final WalletRepositoryPort walletRepository;

    @Override
    @Transactional
    public ReferralCodeDTO getOrCreateReferralCode(String userId) {
        return referralCodeRepository.findByUserId(userId)
                .map(this::toCodeDTO)
                .orElseGet(() -> toCodeDTO(generateAndSave(userId)));
    }

    @Override
    @Transactional
    public ReferralCodeDTO regenerateCode(String userId) {
        referralCodeRepository.findByUserId(userId).ifPresent(existing -> {
            existing.setActive(false);
            referralCodeRepository.save(existing);
        });
        return toCodeDTO(generateAndSave(userId));
    }

    @Override
    @Transactional
    public void registerReferral(String referredId, String code) {
        ReferralCode referralCode = referralCodeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("ReferralCode", code));

        if (!referralCode.isActive()) {
            throw new BadRequestException("Código de indicação inativo");
        }
        if (referralCode.getUserId().equals(referredId)) {
            throw new BadRequestException("Auto-indicação não é permitida");
        }
        if (referralRepository.findByReferredId(referredId).isPresent()) {
            throw new DuplicateResourceException("Referral", "referredId", referredId);
        }

        referralRepository.save(Referral.builder()
                .referrerId(referralCode.getUserId())
                .referredId(referredId)
                .referralCodeId(referralCode.getId())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public ReferralStatsDTO getUserReferralStats(String userId) {
        ReferralCodeDTO codeDTO = getOrCreateReferralCode(userId);
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

    @Override
    @Transactional(readOnly = true)
    public List<ReferralDTO> getAllReferrals() {
        return referralRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ReferralCode generateAndSave(String userId) {
        String code = generateUniqueCode();
        return referralCodeRepository.save(ReferralCode.builder()
                .userId(userId)
                .code(code)
                .build());
    }

    private String generateUniqueCode() {
        for (int attempts = 0; attempts < 10; attempts++) {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            }
            String code = sb.toString();
            if (!referralCodeRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Não foi possível gerar código único");
    }

    private ReferralCodeDTO toCodeDTO(ReferralCode rc) {
        return ReferralCodeDTO.builder()
                .id(rc.getId())
                .userId(rc.getUserId())
                .code(rc.getCode())
                .active(rc.isActive())
                .createdAt(rc.getCreatedAt())
                .build();
    }

    private ReferralDTO toDTO(Referral r) {
        return ReferralDTO.builder()
                .id(r.getId())
                .referrerId(r.getReferrerId())
                .referredId(r.getReferredId())
                .referralCodeId(r.getReferralCodeId())
                .firstPaymentId(r.getFirstPaymentId())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
