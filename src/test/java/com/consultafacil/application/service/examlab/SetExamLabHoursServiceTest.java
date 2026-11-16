package com.consultafacil.application.service.examlab;

import com.consultafacil.api.dto.examlab.ExamLabHoursDTO;
import com.consultafacil.api.dto.examlab.ExamLabResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ExamLab;
import com.consultafacil.domain.entity.ExamLabHours;
import com.consultafacil.domain.port.out.examlab.ExamLabHoursRepositoryPort;
import com.consultafacil.domain.port.out.examlab.ExamLabRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetExamLabHoursServiceTest {

    @Mock ExamLabRepositoryPort examLabRepository;
    @Mock ExamLabHoursRepositoryPort examLabHoursRepository;

    SetExamLabHoursService service;

    @BeforeEach
    void setUp() {
        service = new SetExamLabHoursService(examLabRepository, examLabHoursRepository);
    }

    private ExamLabHoursDTO hours(String day, Integer slot, Boolean open) {
        ExamLabHoursDTO dto = new ExamLabHoursDTO();
        dto.setDayOfWeek(day);
        dto.setOpenTime(LocalTime.of(8, 0));
        dto.setCloseTime(LocalTime.of(17, 0));
        dto.setSlotDurationMinutes(slot);
        dto.setIsOpen(open);
        return dto;
    }

    @Test
    void execute_replacesExistingHoursAndReturnsReloadedLab() {
        ExamLab lab = ExamLab.builder().id("lab-1").name("Lab").build();
        when(examLabRepository.findById("lab-1")).thenReturn(Optional.of(lab));

        ExamLabResponseDTO result = service.execute("lab-1", List.of(
                hours("MONDAY", 15, true),
                hours("SATURDAY", 30, false)));

        InOrder order = inOrder(examLabHoursRepository);
        order.verify(examLabHoursRepository).deleteByExamLabId("lab-1");
        ArgumentCaptor<ExamLabHours> saved = ArgumentCaptor.forClass(ExamLabHours.class);
        order.verify(examLabHoursRepository, times(2)).save(saved.capture());
        assertThat(saved.getAllValues()).extracting(ExamLabHours::getDayOfWeek).containsExactly("MONDAY", "SATURDAY");
        assertThat(saved.getAllValues().get(0).getSlotDurationMinutes()).isEqualTo(15);
        assertThat(saved.getAllValues().get(1).getIsOpen()).isFalse();
        assertThat(saved.getAllValues()).allSatisfy(h -> assertThat(h.getExamLab()).isSameAs(lab));
        verify(examLabRepository, times(2)).findById("lab-1");
        assertThat(result.getId()).isEqualTo("lab-1");
    }

    @Test
    void execute_appliesDefaultsWhenSlotDurationAndOpenFlagAreNull() {
        when(examLabRepository.findById("lab-1")).thenReturn(Optional.of(ExamLab.builder().id("lab-1").name("Lab").build()));

        service.execute("lab-1", List.of(hours("TUESDAY", null, null)));

        ArgumentCaptor<ExamLabHours> saved = ArgumentCaptor.forClass(ExamLabHours.class);
        verify(examLabHoursRepository).save(saved.capture());
        assertThat(saved.getValue().getSlotDurationMinutes()).isEqualTo(30);
        assertThat(saved.getValue().getIsOpen()).isTrue();
    }

    @Test
    void execute_clearsAllHoursWhenListIsEmpty() {
        when(examLabRepository.findById("lab-1")).thenReturn(Optional.of(ExamLab.builder().id("lab-1").name("Lab").build()));

        service.execute("lab-1", List.of());

        verify(examLabHoursRepository).deleteByExamLabId("lab-1");
        verify(examLabHoursRepository, never()).save(any());
    }

    @Test
    void execute_throwsAndTouchesNothingWhenLabDoesNotExist() {
        when(examLabRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("missing", List.of(hours("MONDAY", 30, true))))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(examLabHoursRepository, never()).deleteByExamLabId(any());
        verify(examLabHoursRepository, never()).save(any());
    }
}
