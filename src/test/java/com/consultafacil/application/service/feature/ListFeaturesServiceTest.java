package com.consultafacil.application.service.feature;

import com.consultafacil.api.dto.billing.feature.FeatureResponseDTO;
import com.consultafacil.domain.entity.Feature;
import com.consultafacil.domain.port.out.feature.FeatureRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListFeaturesServiceTest {

    @Mock FeatureRepositoryPort featureRepository;

    ListFeaturesService service;

    Feature consultationsFeature;

    @BeforeEach
    void setUp() {
        consultationsFeature = Feature.builder()
                .id("feat-1")
                .key("CONSULTATIONS")
                .name("Consultas mensais")
                .description("Número de consultas por mês")
                .createdAt(LocalDateTime.now())
                .build();

        service = new ListFeaturesService(featureRepository, new FeatureMapper());
    }

    @Test
    void listAll_returnsAllFeatures() {
        when(featureRepository.findAll()).thenReturn(List.of(consultationsFeature));
        List<FeatureResponseDTO> result = service.execute();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKey()).isEqualTo("CONSULTATIONS");
    }
}
