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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetExamLabsServiceTest {

    @Mock ExamLabRepositoryPort examLabRepository;

    GetExamLabsService service;

    @BeforeEach
    void setUp() {
        service = new GetExamLabsService(examLabRepository);
    }

    @Test
    void execute_listsOnlyActiveLabs() {
        when(examLabRepository.findByStatus("ACTIVE")).thenReturn(List.of(
                ExamLab.builder().id("a").name("Lab A").build(),
                ExamLab.builder().id("b").name("Lab B").build()));

        List<ExamLabResponseDTO> result = service.execute();

        assertThat(result).extracting(ExamLabResponseDTO::getId).containsExactly("a", "b");
        assertThat(result).allSatisfy(lab -> assertThat(lab.getStatus()).isEqualTo("ACTIVE"));
    }

    @Test
    void execute_returnsEmptyListWhenNoActiveLabs() {
        when(examLabRepository.findByStatus("ACTIVE")).thenReturn(List.of());

        assertThat(service.execute()).isEmpty();
    }
}
