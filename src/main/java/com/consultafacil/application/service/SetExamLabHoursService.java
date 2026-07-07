package com.consultafacil.application.service;

import com.consultafacil.api.dto.examlab.ExamLabHoursDTO;
import com.consultafacil.api.dto.examlab.ExamLabResponseDTO;
import com.consultafacil.application.port.in.SetExamLabHoursUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ExamLab;
import com.consultafacil.domain.entity.ExamLabHours;
import com.consultafacil.domain.port.out.ExamLabHoursRepositoryPort;
import com.consultafacil.domain.port.out.ExamLabRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SetExamLabHoursService implements SetExamLabHoursUseCase {

    private final ExamLabRepositoryPort examLabRepository;
    private final ExamLabHoursRepositoryPort examLabHoursRepository;

    @Transactional
    @Override
    public ExamLabResponseDTO execute(String examLabId, List<ExamLabHoursDTO> hours) {
        ExamLab lab = examLabRepository.findById(examLabId)
                .orElseThrow(() -> new ResourceNotFoundException("ExamLab", examLabId));

        examLabHoursRepository.deleteByExamLabId(examLabId);

        for (ExamLabHoursDTO dto : hours) {
            examLabHoursRepository.save(ExamLabHours.builder()
                    .examLab(lab)
                    .dayOfWeek(dto.getDayOfWeek())
                    .openTime(dto.getOpenTime())
                    .closeTime(dto.getCloseTime())
                    .slotDurationMinutes(dto.getSlotDurationMinutes() != null ? dto.getSlotDurationMinutes() : 30)
                    .isOpen(dto.getIsOpen() != null ? dto.getIsOpen() : true)
                    .build());
        }

        ExamLab reloaded = examLabRepository.findById(examLabId)
                .orElseThrow(() -> new ResourceNotFoundException("ExamLab", examLabId));

        return CreateExamLabService.toDTO(reloaded);
    }
}
