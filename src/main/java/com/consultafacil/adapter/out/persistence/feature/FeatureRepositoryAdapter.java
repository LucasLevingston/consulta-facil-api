package com.consultafacil.adapter.out.persistence.feature;

import com.consultafacil.domain.entity.Feature;
import com.consultafacil.domain.port.out.feature.FeatureRepositoryPort;
import com.consultafacil.domain.repository.feature.FeatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FeatureRepositoryAdapter implements FeatureRepositoryPort {

    private final FeatureRepository featureRepository;

    @Override
    public Feature save(Feature feature) { return featureRepository.save(feature); }

    @Override
    public Optional<Feature> findById(String id) { return featureRepository.findById(id); }

    @Override
    public Optional<Feature> findByKey(String key) { return featureRepository.findByKey(key); }

    @Override
    public List<Feature> findAll() { return featureRepository.findAll(); }

    @Override
    public void deleteById(String id) { featureRepository.deleteById(id); }

    @Override
    public boolean existsByKey(String key) { return featureRepository.existsByKey(key); }
}
