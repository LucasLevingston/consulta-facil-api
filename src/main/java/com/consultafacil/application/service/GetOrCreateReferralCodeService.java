package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.referral.ReferralCodeDTO;
import com.consultafacil.application.port.in.GetOrCreateReferralCodeUseCase;
import com.consultafacil.domain.port.out.ReferralCodeRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetOrCreateReferralCodeService implements GetOrCreateReferralCodeUseCase {

    private final ReferralCodeRepositoryPort referralCodeRepository;
    private final ReferralCodeGenerator codeGenerator;
    private final ReferralCodeMapper mapper;

    @Override
    @Transactional
    public ReferralCodeDTO execute(String userId) {
        return referralCodeRepository.findByUserId(userId)
                .map(mapper::toDTO)
                .orElseGet(() -> mapper.toDTO(codeGenerator.generateAndSave(userId)));
    }
}
