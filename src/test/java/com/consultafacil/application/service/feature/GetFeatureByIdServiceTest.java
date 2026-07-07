package com.consultafacil.application.service.feature;

import com.consultafacil.api.dto.billing.feature.FeatureResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Feature;
import com.consultafacil.domain.port.out.FeatureRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFeatureByIdServiceTest {

    @Mock FeatureRepositoryPort featureRepository;

    GetFeatureByIdService service;

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

        service = new GetFeatureByIdService(featureRepository, new FeatureMapper());
    }

    @Test
    void getById_found_returnsDTO() {
        when(featureRepository.findById("feat-1")).thenReturn(Optional.of(consultationsFeature));
        FeatureResponseDTO result = service.execute("feat-1");
        assertThat(result.getId()).isEqualTo("feat-1");
    }

    @Test
    void getById_notFound_throws() {
        when(featureRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.execute("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
