package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.systemfee.SystemFeeResponseDTO;
import com.consultafacil.api.dto.billing.systemfee.UpdateSystemFeeDTO;
import com.consultafacil.application.port.in.UpdateSystemFeeUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.SystemFee;
import com.consultafacil.domain.port.out.SystemFeeRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateSystemFeeService implements UpdateSystemFeeUseCase {

    private final SystemFeeRepositoryPort systemFeeRepository;
    private final SystemFeeMapper mapper;

    @Override
    @Transactional
    public SystemFeeResponseDTO execute(String id, UpdateSystemFeeDTO dto) {
        SystemFee fee = systemFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SystemFee", id));
        if (dto.getFixedFee() != null) fee.setFixedFee(dto.getFixedFee());
        if (dto.getPercentageFee() != null) fee.setPercentageFee(dto.getPercentageFee());
        if (dto.getActive() != null) fee.setActive(dto.getActive());
        return mapper.toDTO(systemFeeRepository.save(fee));
    }
}
