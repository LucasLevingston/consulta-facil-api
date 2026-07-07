package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.referral.ReferralDTO;
import com.consultafacil.application.port.in.GetAllReferralsUseCase;
import com.consultafacil.domain.port.out.ReferralRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAllReferralsService implements GetAllReferralsUseCase {

    private final ReferralRepositoryPort referralRepository;
    private final ReferralMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ReferralDTO> execute() {
        return referralRepository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}
