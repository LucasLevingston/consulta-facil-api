package com.consultafacil.application.service.examlab;

import com.consultafacil.api.dto.examlab.ExamLabResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ExamLab;
import com.consultafacil.domain.port.out.examlab.ExamLabRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetExamLabByIdServiceTest {

    @Mock ExamLabRepositoryPort examLabRepository;

    GetExamLabByIdService service;

    @BeforeEach
    void setUp() {
        service = new GetExamLabByIdService(examLabRepository);
    }

    @Test
    void execute_returnsMappedLabWhenFound() {
        when(examLabRepository.findById("lab-1"))
                .thenReturn(Optional.of(ExamLab.builder().id("lab-1").name("Lab Norte").city("Recife").build()));

        ExamLabResponseDTO result = service.execute("lab-1");

        assertThat(result.getId()).isEqualTo("lab-1");
        assertThat(result.getName()).isEqualTo("Lab Norte");
        assertThat(result.getCity()).isEqualTo("Recife");
    }

    @Test
    void execute_throwsWhenLabDoesNotExist() {
        when(examLabRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ExamLab")
                .hasMessageContaining("missing");
    }
}
