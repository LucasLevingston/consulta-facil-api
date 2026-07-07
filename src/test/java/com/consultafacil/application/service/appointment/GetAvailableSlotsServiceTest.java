package com.consultafacil.application.service.appointment;

import com.consultafacil.api.dto.examlab.AvailableSlotDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ExamLab;
import com.consultafacil.domain.entity.ExamLabHours;
import com.consultafacil.domain.entity.ExamScheduling;
import com.consultafacil.domain.enums.ExamSchedulingStatus;
import com.consultafacil.domain.port.out.ExamLabHoursRepositoryPort;
import com.consultafacil.domain.port.out.ExamLabRepositoryPort;
import com.consultafacil.domain.port.out.ExamSchedulingRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAvailableSlotsServiceTest {

    @Mock ExamLabRepositoryPort examLabRepository;
    @Mock ExamLabHoursRepositoryPort examLabHoursRepository;
    @Mock ExamSchedulingRepositoryPort examSchedulingRepository;

    @InjectMocks GetAvailableSlotsService service;

    ExamLab lab;
    ExamLabHours hours;
    LocalDate monday;

    @BeforeEach
    void setUp() {
        lab = ExamLab.builder().id("lab-1").name("Lab Teste").status("ACTIVE").build();

        hours = ExamLabHours.builder()
                .id("hours-1")
                .examLab(lab)
                .dayOfWeek("MONDAY")
                .openTime(LocalTime.of(8, 0))
                .closeTime(LocalTime.of(10, 0))
                .slotDurationMinutes(30)
                .isOpen(true)
                .build();

        monday = LocalDate.of(2026, 6, 15);
    }

    @Test
    void execute_labNotFound_throwsResourceNotFoundException() {
        when(examLabRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("bad-id", monday))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_noHoursForDay_returnsEmptyList() {
        when(examLabRepository.findById("lab-1")).thenReturn(Optional.of(lab));
        when(examLabHoursRepository.findByExamLabIdAndDayOfWeek("lab-1", "MONDAY"))
                .thenReturn(Optional.empty());

        var result = service.execute("lab-1", monday);

        assertThat(result).isEmpty();
    }

    @Test
    void execute_labClosed_returnsEmptyList() {
        hours.setIsOpen(false);
        when(examLabRepository.findById("lab-1")).thenReturn(Optional.of(lab));
        when(examLabHoursRepository.findByExamLabIdAndDayOfWeek("lab-1", "MONDAY"))
                .thenReturn(Optional.of(hours));

        var result = service.execute("lab-1", monday);

        assertThat(result).isEmpty();
    }

    @Test
    void execute_noBookings_allSlotsAvailable() {
        when(examLabRepository.findById("lab-1")).thenReturn(Optional.of(lab));
        when(examLabHoursRepository.findByExamLabIdAndDayOfWeek("lab-1", "MONDAY"))
                .thenReturn(Optional.of(hours));
        when(examSchedulingRepository.findByExamLabIdAndScheduledDate("lab-1", monday))
                .thenReturn(List.of());

        var result = service.execute("lab-1", monday);

        assertThat(result).hasSize(4);
        assertThat(result.stream().allMatch(AvailableSlotDTO::isAvailable)).isTrue();
        assertThat(result.get(0).getTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(result.get(3).getTime()).isEqualTo(LocalTime.of(9, 30));
    }

    @Test
    void execute_withBookedSlot_markedUnavailable() {
        ExamScheduling booked = ExamScheduling.builder()
                .id("sched-1")
                .examLab(lab)
                .scheduledDate(monday)
                .scheduledTime(LocalTime.of(8, 0))
                .status(ExamSchedulingStatus.SCHEDULED)
                .build();

        when(examLabRepository.findById("lab-1")).thenReturn(Optional.of(lab));
        when(examLabHoursRepository.findByExamLabIdAndDayOfWeek("lab-1", "MONDAY"))
                .thenReturn(Optional.of(hours));
        when(examSchedulingRepository.findByExamLabIdAndScheduledDate("lab-1", monday))
                .thenReturn(List.of(booked));

        var result = service.execute("lab-1", monday);

        assertThat(result).hasSize(4);
        assertThat(result.get(0).getTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(result.get(0).isAvailable()).isFalse();
        assertThat(result.get(1).isAvailable()).isTrue();
    }

    @Test
    void execute_cancelledBooking_slotRemainsAvailable() {
        ExamScheduling cancelled = ExamScheduling.builder()
                .id("sched-1")
                .examLab(lab)
                .scheduledDate(monday)
                .scheduledTime(LocalTime.of(8, 0))
                .status(ExamSchedulingStatus.CANCELLED)
                .build();

        when(examLabRepository.findById("lab-1")).thenReturn(Optional.of(lab));
        when(examLabHoursRepository.findByExamLabIdAndDayOfWeek("lab-1", "MONDAY"))
                .thenReturn(Optional.of(hours));
        when(examSchedulingRepository.findByExamLabIdAndScheduledDate("lab-1", monday))
                .thenReturn(List.of(cancelled));

        var result = service.execute("lab-1", monday);

        assertThat(result.get(0).isAvailable()).isTrue();
    }
}
