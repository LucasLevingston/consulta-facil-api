package com.consultafacil.application.service.systemfee;

import com.consultafacil.api.dto.billing.systemfee.SystemFeeResponseDTO;
import com.consultafacil.application.port.in.systemfee.ListSystemFeesUseCase;
import com.consultafacil.domain.port.out.systemfee.SystemFeeRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListSystemFeesService implements ListSystemFeesUseCase {

    private final SystemFeeRepositoryPort systemFeeRepository;
    private final SystemFeeMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<SystemFeeResponseDTO> execute() {
        return systemFeeRepository.findAll().stream().map(mapper::toDTO).collect(Collectors.toList());
    }
}
