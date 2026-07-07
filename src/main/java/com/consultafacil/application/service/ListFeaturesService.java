package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.feature.FeatureResponseDTO;
import com.consultafacil.application.port.in.ListFeaturesUseCase;
import com.consultafacil.domain.port.out.FeatureRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListFeaturesService implements ListFeaturesUseCase {

    private final FeatureRepositoryPort featureRepository;
    private final FeatureMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<FeatureResponseDTO> execute() {
        return featureRepository.findAll().stream().map(mapper::toDTO).collect(Collectors.toList());
    }
}
