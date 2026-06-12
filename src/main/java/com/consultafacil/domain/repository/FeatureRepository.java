package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.Feature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeatureRepository extends JpaRepository<Feature, String> {
    Optional<Feature> findByKey(String key);
    boolean existsByKey(String key);
}
