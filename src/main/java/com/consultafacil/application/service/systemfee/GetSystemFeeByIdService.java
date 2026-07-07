package com.consultafacil.application.service.systemfee;

import com.consultafacil.api.dto.billing.systemfee.SystemFeeResponseDTO;
import com.consultafacil.application.port.in.GetSystemFeeByIdUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.port.out.SystemFeeRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSystemFeeByIdService implements GetSystemFeeByIdUseCase {

    private final SystemFeeRepositoryPort systemFeeRepository;
    private final SystemFeeMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public SystemFeeResponseDTO execute(String id) {
        return systemFeeRepository.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("SystemFee", id));
    }
}
