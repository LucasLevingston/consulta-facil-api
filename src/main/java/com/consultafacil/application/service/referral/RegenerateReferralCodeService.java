package com.consultafacil.application.service.referral;

import com.consultafacil.api.dto.billing.referral.ReferralCodeDTO;
import com.consultafacil.application.port.in.RegenerateReferralCodeUseCase;
import com.consultafacil.domain.port.out.ReferralCodeRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegenerateReferralCodeService implements RegenerateReferralCodeUseCase {

    private final ReferralCodeRepositoryPort referralCodeRepository;
    private final ReferralCodeGenerator codeGenerator;
    private final ReferralCodeMapper mapper;

    @Override
    @Transactional
    public ReferralCodeDTO execute(String userId) {
        referralCodeRepository.findByUserId(userId).ifPresent(existing -> {
            existing.setActive(false);
            referralCodeRepository.save(existing);
        });
        return mapper.toDTO(codeGenerator.generateAndSave(userId));
    }
}
