package com.consultafacil.application.service.feature;

import com.consultafacil.api.dto.billing.feature.FeatureResponseDTO;
import com.consultafacil.api.dto.billing.feature.UpdateFeatureDTO;
import com.consultafacil.application.port.in.feature.UpdateFeatureUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Feature;
import com.consultafacil.domain.port.out.feature.FeatureRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateFeatureService implements UpdateFeatureUseCase {

    private final FeatureRepositoryPort featureRepository;
    private final FeatureMapper mapper;

    @Override
    @Transactional
    public FeatureResponseDTO execute(String id, UpdateFeatureDTO dto) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature", id));
        if (dto.getName() != null) feature.setName(dto.getName());
        if (dto.getDescription() != null) feature.setDescription(dto.getDescription());
        return mapper.toDTO(featureRepository.save(feature));
    }
}
