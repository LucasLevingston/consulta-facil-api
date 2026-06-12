package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.feature.CreateFeatureDTO;
import com.consultafacil.api.dto.billing.feature.FeatureResponseDTO;
import com.consultafacil.api.dto.billing.feature.UpdateFeatureDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Feature;
import com.consultafacil.domain.port.out.FeatureRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FeatureServiceTest {

    @Mock FeatureRepositoryPort featureRepository;

    @InjectMocks FeatureService service;

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

        when(featureRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void listAll_returnsAllFeatures() {
        when(featureRepository.findAll()).thenReturn(List.of(consultationsFeature));
        List<FeatureResponseDTO> result = service.listAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKey()).isEqualTo("CONSULTATIONS");
    }

    @Test
    void getById_found_returnsDTO() {
        when(featureRepository.findById("feat-1")).thenReturn(Optional.of(consultationsFeature));
        FeatureResponseDTO result = service.getById("feat-1");
        assertThat(result.getId()).isEqualTo("feat-1");
    }

    @Test
    void getById_notFound_throws() {
        when(featureRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_normalizesKey() {
        CreateFeatureDTO dto = new CreateFeatureDTO();
        dto.setKey("max users");
        dto.setName("Usuários máximos");

        FeatureResponseDTO result = service.create(dto);

        assertThat(result.getKey()).isEqualTo("MAX_USERS");
    }

    @Test
    void update_changesNameAndDescription() {
        when(featureRepository.findById("feat-1")).thenReturn(Optional.of(consultationsFeature));

        UpdateFeatureDTO dto = new UpdateFeatureDTO();
        dto.setName("Consultas");
        dto.setDescription("Atualizado");

        FeatureResponseDTO result = service.update("feat-1", dto);

        assertThat(result.getName()).isEqualTo("Consultas");
        assertThat(result.getDescription()).isEqualTo("Atualizado");
    }

    @Test
    void delete_notFound_throws() {
        when(featureRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_found_callsDelete() {
        when(featureRepository.findById("feat-1")).thenReturn(Optional.of(consultationsFeature));
        service.delete("feat-1");
        verify(featureRepository).deleteById("feat-1");
    }
}
