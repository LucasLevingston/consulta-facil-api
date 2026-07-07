package com.consultafacil.application.service.examlab;

import com.consultafacil.api.dto.examlab.CreateExamLabDTO;
import com.consultafacil.api.dto.examlab.ExamLabResponseDTO;
import com.consultafacil.application.port.in.CreateExamLabUseCase;
import com.consultafacil.domain.entity.ExamLab;
import com.consultafacil.domain.port.out.ExamLabRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateExamLabService implements CreateExamLabUseCase {

    private final ExamLabRepositoryPort examLabRepository;

    @Transactional
    @Override
    public ExamLabResponseDTO execute(CreateExamLabDTO dto) {
        ExamLab lab = ExamLab.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .imageUrl(dto.getImageUrl())
                .acceptedExams(dto.getAcceptedExams() != null ? new ArrayList<>(dto.getAcceptedExams()) : new ArrayList<>())
                .build();

        return toDTO(examLabRepository.save(lab));
    }

    static ExamLabResponseDTO toDTO(ExamLab lab) {
        return ExamLabResponseDTO.builder()
                .id(lab.getId())
                .name(lab.getName())
                .description(lab.getDescription())
                .phone(lab.getPhone())
                .address(lab.getAddress())
                .city(lab.getCity())
                .state(lab.getState())
                .zipCode(lab.getZipCode())
                .latitude(lab.getLatitude())
                .longitude(lab.getLongitude())
                .imageUrl(lab.getImageUrl())
                .acceptedExams(lab.getAcceptedExams() != null
                        ? lab.getAcceptedExams().stream().map(Enum::name).toList()
                        : List.of())
                .status(lab.getStatus())
                .hours(lab.getHours() == null ? java.util.List.of() : lab.getHours().stream()
                        .map(h -> ExamLabResponseDTO.HoursEntry.builder()
                                .id(h.getId())
                                .dayOfWeek(h.getDayOfWeek())
                                .openTime(h.getOpenTime())
                                .closeTime(h.getCloseTime())
                                .slotDurationMinutes(h.getSlotDurationMinutes())
                                .isOpen(h.getIsOpen())
                                .build())
                        .toList())
                .createdAt(lab.getCreatedAt())
                .build();
    }
}
