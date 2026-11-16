package com.consultafacil.application.service.examlab;

import com.consultafacil.api.dto.examlab.CreateExamLabDTO;
import com.consultafacil.api.dto.examlab.ExamLabResponseDTO;
import com.consultafacil.domain.entity.ExamLab;
import com.consultafacil.domain.entity.ExamLabHours;
import com.consultafacil.domain.enums.ExamType;
import com.consultafacil.domain.port.out.examlab.ExamLabRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateExamLabServiceTest {

    @Mock ExamLabRepositoryPort examLabRepository;

    CreateExamLabService service;

    @BeforeEach
    void setUp() {
        service = new CreateExamLabService(examLabRepository);
    }

    private CreateExamLabDTO dto() {
        CreateExamLabDTO dto = new CreateExamLabDTO();
        dto.setName("Lab Central");
        dto.setDescription("Exames de rotina");
        dto.setPhone("11999990000");
        dto.setAddress("Rua A, 10");
        dto.setCity("São Paulo");
        dto.setState("SP");
        dto.setZipCode("01000-000");
        dto.setLatitude(-23.55);
        dto.setLongitude(-46.63);
        dto.setImageUrl("https://img/lab.png");
        dto.setAcceptedExams(List.of(ExamType.HEMOGRAMA_COMPLETO, ExamType.GLICEMIA_JEJUM));
        return dto;
    }

    @Test
    void execute_persistsLabWithAllFieldsFromDto() {
        when(examLabRepository.save(any())).thenAnswer(inv -> {
            ExamLab lab = inv.getArgument(0);
            lab.setId("lab-1");
            return lab;
        });

        ExamLabResponseDTO result = service.execute(dto());

        ArgumentCaptor<ExamLab> saved = ArgumentCaptor.forClass(ExamLab.class);
        verify(examLabRepository).save(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo("Lab Central");
        assertThat(saved.getValue().getCity()).isEqualTo("São Paulo");
        assertThat(saved.getValue().getLatitude()).isEqualTo(-23.55);
        assertThat(saved.getValue().getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getId()).isEqualTo("lab-1");
        assertThat(result.getAcceptedExams()).containsExactly("HEMOGRAMA_COMPLETO", "GLICEMIA_JEJUM");
        assertThat(result.getHours()).isEmpty();
    }

    @Test
    void execute_defaultsAcceptedExamsToEmptyMutableListWhenNull() {
        CreateExamLabDTO dto = dto();
        dto.setAcceptedExams(null);
        when(examLabRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ExamLabResponseDTO result = service.execute(dto);

        ArgumentCaptor<ExamLab> saved = ArgumentCaptor.forClass(ExamLab.class);
        verify(examLabRepository).save(saved.capture());
        assertThat(saved.getValue().getAcceptedExams()).isEmpty();
        saved.getValue().getAcceptedExams().add(ExamType.HEMOGRAMA_COMPLETO);
        assertThat(result.getAcceptedExams()).isEmpty();
    }

    @Test
    void toDTO_mapsOpeningHoursEntries() {
        ExamLab lab = ExamLab.builder().id("lab-9").name("Lab").build();
        lab.setHours(new ArrayList<>(List.of(ExamLabHours.builder()
                .id("h-1")
                .examLab(lab)
                .dayOfWeek("MONDAY")
                .openTime(LocalTime.of(7, 0))
                .closeTime(LocalTime.of(12, 0))
                .slotDurationMinutes(20)
                .isOpen(true)
                .build())));

        ExamLabResponseDTO dto = CreateExamLabService.toDTO(lab);

        assertThat(dto.getHours()).singleElement().satisfies(h -> {
            assertThat(h.getId()).isEqualTo("h-1");
            assertThat(h.getDayOfWeek()).isEqualTo("MONDAY");
            assertThat(h.getOpenTime()).isEqualTo(LocalTime.of(7, 0));
            assertThat(h.getCloseTime()).isEqualTo(LocalTime.of(12, 0));
            assertThat(h.getSlotDurationMinutes()).isEqualTo(20);
            assertThat(h.getIsOpen()).isTrue();
        });
    }

    @Test
    void toDTO_handlesNullCollections() {
        ExamLab lab = ExamLab.builder().id("lab-2").name("Lab").build();
        lab.setAcceptedExams(null);
        lab.setHours(null);

        ExamLabResponseDTO dto = CreateExamLabService.toDTO(lab);

        assertThat(dto.getAcceptedExams()).isEmpty();
        assertThat(dto.getHours()).isEmpty();
    }
}
