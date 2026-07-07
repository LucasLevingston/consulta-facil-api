package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.feature.FeatureResponseDTO;
import com.consultafacil.application.port.in.GetFeatureByIdUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.port.out.FeatureRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetFeatureByIdService implements GetFeatureByIdUseCase {

    private final FeatureRepositoryPort featureRepository;
    private final FeatureMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public FeatureResponseDTO execute(String id) {
        return featureRepository.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Feature", id));
    }
}
