package com.consultafacil.application.service.appointment;

import com.consultafacil.api.dto.examlab.AvailableSlotDTO;
import com.consultafacil.application.port.in.appointment.GetAvailableSlotsUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ExamScheduling;
import com.consultafacil.domain.enums.ExamSchedulingStatus;
import com.consultafacil.domain.port.out.examlab.ExamLabHoursRepositoryPort;
import com.consultafacil.domain.port.out.examlab.ExamLabRepositoryPort;
import com.consultafacil.domain.port.out.examrequest.ExamSchedulingRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAvailableSlotsService implements GetAvailableSlotsUseCase {

    private final ExamLabRepositoryPort examLabRepository;
    private final ExamLabHoursRepositoryPort examLabHoursRepository;
    private final ExamSchedulingRepositoryPort examSchedulingRepository;

    @Transactional(readOnly = true)
    @Override
    public List<AvailableSlotDTO> execute(String examLabId, LocalDate date) {
        examLabRepository.findById(examLabId)
                .orElseThrow(() -> new ResourceNotFoundException("ExamLab", examLabId));

        String dayOfWeek = date.getDayOfWeek().name();

        var hoursOpt = examLabHoursRepository.findByExamLabIdAndDayOfWeek(examLabId, dayOfWeek);
        if (hoursOpt.isEmpty() || !hoursOpt.get().getIsOpen()) {
            return List.of();
        }

        var hours = hoursOpt.get();
        int durationMinutes = hours.getSlotDurationMinutes();

        Set<LocalTime> bookedTimes = examSchedulingRepository
                .findByExamLabIdAndScheduledDate(examLabId, date)
                .stream()
                .filter(s -> s.getStatus() == ExamSchedulingStatus.SCHEDULED)
                .map(ExamScheduling::getScheduledTime)
                .collect(Collectors.toSet());

        List<AvailableSlotDTO> slots = new ArrayList<>();
        LocalTime current = hours.getOpenTime();

        while (current.isBefore(hours.getCloseTime())) {
            slots.add(AvailableSlotDTO.builder()
                    .time(current)
                    .available(!bookedTimes.contains(current))
                    .build());
            current = current.plusMinutes(durationMinutes);
        }

        return slots;
    }
}
