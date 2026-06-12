package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.feature.CreateFeatureDTO;
import com.consultafacil.api.dto.billing.feature.FeatureResponseDTO;
import com.consultafacil.api.dto.billing.feature.UpdateFeatureDTO;
import com.consultafacil.application.port.in.FeatureUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Feature;
import com.consultafacil.domain.port.out.FeatureRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeatureService implements FeatureUseCase {

    private final FeatureRepositoryPort featureRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FeatureResponseDTO> listAll() {
        return featureRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FeatureResponseDTO getById(String id) {
        return featureRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Feature", id));
    }

    @Override
    @Transactional
    public FeatureResponseDTO create(CreateFeatureDTO dto) {
        Feature feature = Feature.builder()
                .key(dto.getKey().toUpperCase().replace(" ", "_"))
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
        return toDTO(featureRepository.save(feature));
    }

    @Override
    @Transactional
    public FeatureResponseDTO update(String id, UpdateFeatureDTO dto) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature", id));
        if (dto.getName() != null) feature.setName(dto.getName());
        if (dto.getDescription() != null) feature.setDescription(dto.getDescription());
        return toDTO(featureRepository.save(feature));
    }

    @Override
    @Transactional
    public void delete(String id) {
        if (!featureRepository.findById(id).isPresent()) {
            throw new ResourceNotFoundException("Feature", id);
        }
        featureRepository.deleteById(id);
    }

    private FeatureResponseDTO toDTO(Feature f) {
        return FeatureResponseDTO.builder()
                .id(f.getId())
                .key(f.getKey())
                .name(f.getName())
                .description(f.getDescription())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
