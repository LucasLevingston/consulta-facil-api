package com.consultafacil.application.service.feature;

import com.consultafacil.api.dto.billing.feature.CreateFeatureDTO;
import com.consultafacil.api.dto.billing.feature.FeatureResponseDTO;
import com.consultafacil.domain.port.out.feature.FeatureRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateFeatureServiceTest {

    @Mock FeatureRepositoryPort featureRepository;

    CreateFeatureService service;

    @BeforeEach
    void setUp() {
        service = new CreateFeatureService(featureRepository, new FeatureMapper());
        when(featureRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void create_normalizesKey() {
        CreateFeatureDTO dto = new CreateFeatureDTO();
        dto.setKey("max users");
        dto.setName("Usuários máximos");

        FeatureResponseDTO result = service.execute(dto);

        assertThat(result.getKey()).isEqualTo("MAX_USERS");
    }
}
