package com.consultafacil.application.service.examlab;

import com.consultafacil.api.dto.examlab.ExamLabResponseDTO;
import com.consultafacil.domain.entity.ExamLab;
import com.consultafacil.domain.port.out.examlab.ExamLabRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetNearbyExamLabsServiceTest {

    @Mock ExamLabRepositoryPort examLabRepository;

    GetNearbyExamLabsService service;

    @BeforeEach
    void setUp() {
        service = new GetNearbyExamLabsService(examLabRepository);
    }

    @Test
    void execute_delegatesCoordinatesAndRadiusToRepository() {
        when(examLabRepository.findNearby(-8.05, -34.9, 15.0)).thenReturn(List.of(
                ExamLab.builder().id("near").name("Lab Perto").latitude(-8.06).longitude(-34.91).build()));

        List<ExamLabResponseDTO> result = service.execute(-8.05, -34.9, 15.0);

        verify(examLabRepository).findNearby(-8.05, -34.9, 15.0);
        assertThat(result).singleElement().satisfies(lab -> {
            assertThat(lab.getId()).isEqualTo("near");
            assertThat(lab.getLatitude()).isEqualTo(-8.06);
        });
    }

    @Test
    void execute_returnsEmptyListWhenNothingIsNearby() {
        when(examLabRepository.findNearby(0.0, 0.0, 1.0)).thenReturn(List.of());

        assertThat(service.execute(0.0, 0.0, 1.0)).isEmpty();
    }
}
