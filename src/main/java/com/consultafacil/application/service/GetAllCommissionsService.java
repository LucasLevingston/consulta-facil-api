package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.commission.ReferralCommissionDTO;
import com.consultafacil.application.port.in.GetAllCommissionsUseCase;
import com.consultafacil.domain.port.out.ReferralCommissionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAllCommissionsService implements GetAllCommissionsUseCase {

    private final ReferralCommissionRepositoryPort commissionRepository;
    private final ReferralCommissionMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ReferralCommissionDTO> execute() {
        return commissionRepository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}
