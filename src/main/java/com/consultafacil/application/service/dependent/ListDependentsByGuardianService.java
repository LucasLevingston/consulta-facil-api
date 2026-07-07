package com.consultafacil.application.service.dependent;

import com.consultafacil.api.dto.dependent.DependentResponseDTO;
import com.consultafacil.application.port.in.ListDependentsByGuardianUseCase;
import com.consultafacil.domain.port.out.DependentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListDependentsByGuardianService implements ListDependentsByGuardianUseCase {

    private final DependentRepositoryPort dependentRepository;
    private final DependentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<DependentResponseDTO> execute(String guardianUserId) {
        return dependentRepository.findByGuardianId(guardianUserId).stream()
                .map(mapper::toDTO)
                .toList();
    }
}
