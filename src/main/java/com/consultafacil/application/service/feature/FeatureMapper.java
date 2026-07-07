package com.consultafacil.application.service.feature;

import com.consultafacil.api.dto.billing.feature.FeatureResponseDTO;
import com.consultafacil.domain.entity.Feature;
import org.springframework.stereotype.Component;

@Component
public class FeatureMapper {

    public FeatureResponseDTO toDTO(Feature f) {
        return FeatureResponseDTO.builder()
                .id(f.getId())
                .key(f.getKey())
                .name(f.getName())
                .description(f.getDescription())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
