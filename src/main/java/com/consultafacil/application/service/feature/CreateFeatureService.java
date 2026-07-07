package com.consultafacil.application.service.feature;

import com.consultafacil.api.dto.billing.feature.CreateFeatureDTO;
import com.consultafacil.api.dto.billing.feature.FeatureResponseDTO;
import com.consultafacil.application.port.in.feature.CreateFeatureUseCase;
import com.consultafacil.domain.entity.Feature;
import com.consultafacil.domain.port.out.feature.FeatureRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateFeatureService implements CreateFeatureUseCase {

    private final FeatureRepositoryPort featureRepository;
    private final FeatureMapper mapper;

    @Override
    @Transactional
    public FeatureResponseDTO execute(CreateFeatureDTO dto) {
        Feature feature = Feature.builder()
                .key(dto.getKey().toUpperCase().replace(" ", "_"))
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
        return mapper.toDTO(featureRepository.save(feature));
    }
}
