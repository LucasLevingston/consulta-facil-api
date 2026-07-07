package com.consultafacil.application.service.feature;

import com.consultafacil.api.dto.billing.feature.FeatureResponseDTO;
import com.consultafacil.api.dto.billing.feature.UpdateFeatureDTO;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateFeatureServiceTest {

    @Mock FeatureRepositoryPort featureRepository;

    UpdateFeatureService service;

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

        service = new UpdateFeatureService(featureRepository, new FeatureMapper());
        when(featureRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void update_changesNameAndDescription() {
        when(featureRepository.findById("feat-1")).thenReturn(Optional.of(consultationsFeature));

        UpdateFeatureDTO dto = new UpdateFeatureDTO();
        dto.setName("Consultas");
        dto.setDescription("Atualizado");

        FeatureResponseDTO result = service.execute("feat-1", dto);

        assertThat(result.getName()).isEqualTo("Consultas");
        assertThat(result.getDescription()).isEqualTo("Atualizado");
    }
}
