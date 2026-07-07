package com.consultafacil.application.service.referral;

import com.consultafacil.application.port.in.referral.RegisterReferralUseCase;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.DuplicateResourceException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Referral;
import com.consultafacil.domain.entity.ReferralCode;
import com.consultafacil.domain.port.out.referral.ReferralCodeRepositoryPort;
import com.consultafacil.domain.port.out.referral.ReferralRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterReferralService implements RegisterReferralUseCase {

    private final ReferralCodeRepositoryPort referralCodeRepository;
    private final ReferralRepositoryPort referralRepository;

    @Override
    @Transactional
    public void execute(String referredId, String code) {
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
}
