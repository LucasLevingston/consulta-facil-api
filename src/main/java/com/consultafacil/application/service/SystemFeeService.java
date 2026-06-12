package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.systemfee.SystemFeeResponseDTO;
import com.consultafacil.api.dto.billing.systemfee.UpdateSystemFeeDTO;
import com.consultafacil.application.port.in.SystemFeeUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.SystemFee;
import com.consultafacil.domain.port.out.SystemFeeRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemFeeService implements SystemFeeUseCase {

    private final SystemFeeRepositoryPort systemFeeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SystemFeeResponseDTO> listAll() {
        return systemFeeRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SystemFeeResponseDTO getById(String id) {
        return systemFeeRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("SystemFee", id));
    }

    @Override
    @Transactional
    public SystemFeeResponseDTO update(String id, UpdateSystemFeeDTO dto) {
        SystemFee fee = systemFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SystemFee", id));
        if (dto.getFixedFee() != null) fee.setFixedFee(dto.getFixedFee());
        if (dto.getPercentageFee() != null) fee.setPercentageFee(dto.getPercentageFee());
        if (dto.getActive() != null) fee.setActive(dto.getActive());
        return toDTO(systemFeeRepository.save(fee));
    }

    private SystemFeeResponseDTO toDTO(SystemFee f) {
        return SystemFeeResponseDTO.builder()
                .id(f.getId())
                .paymentType(f.getPaymentType())
                .fixedFee(f.getFixedFee())
                .percentageFee(f.getPercentageFee())
                .active(f.isActive())
                .updatedAt(f.getUpdatedAt())
                .build();
    }
}
