package com.consultafacil.application.service.plan;

import com.consultafacil.api.dto.plan.PlanResponseDTO;
import com.consultafacil.application.port.in.plan.ListAllPlansUseCase;
import com.consultafacil.domain.port.out.plan.PlanRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListAllPlansService implements ListAllPlansUseCase {

    private final PlanRepositoryPort planRepository;
    private final PlanMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponseDTO> execute() {
        return planRepository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}
