package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.Feature;

import java.util.List;
import java.util.Optional;

public interface FeatureRepositoryPort {
    Feature save(Feature feature);
    Optional<Feature> findById(String id);
    Optional<Feature> findByKey(String key);
    List<Feature> findAll();
    void deleteById(String id);
    boolean existsByKey(String key);
}
