package com.consultafacil.application.service.feature;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Feature;
import com.consultafacil.domain.port.out.feature.FeatureRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteFeatureServiceTest {

    @Mock FeatureRepositoryPort featureRepository;

    DeleteFeatureService service;

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

        service = new DeleteFeatureService(featureRepository);
    }

    @Test
    void delete_notFound_throws() {
        when(featureRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.execute("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_found_callsDelete() {
        when(featureRepository.findById("feat-1")).thenReturn(Optional.of(consultationsFeature));
        service.execute("feat-1");
        verify(featureRepository).deleteById("feat-1");
    }
}
