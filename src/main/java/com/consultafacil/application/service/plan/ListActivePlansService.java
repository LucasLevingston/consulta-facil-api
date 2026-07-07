package com.consultafacil.application.service.plan;

import com.consultafacil.api.dto.plan.PlanResponseDTO;
import com.consultafacil.application.port.in.plan.ListActivePlansUseCase;
import com.consultafacil.domain.port.out.plan.PlanRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListActivePlansService implements ListActivePlansUseCase {

    private final PlanRepositoryPort planRepository;
    private final PlanMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponseDTO> execute() {
        return planRepository.findAllActiveOrderByDisplayOrder().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}
