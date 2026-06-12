package com.consultafacil.application.service;

import com.consultafacil.api.dto.examscheduling.ExamSchedulingResponseDTO;
import com.consultafacil.api.dto.examscheduling.ScheduleExamDTO;
import com.consultafacil.application.port.in.ScheduleExamUseCase;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ExamLab;
import com.consultafacil.domain.entity.ExamRequest;
import com.consultafacil.domain.entity.ExamScheduling;
import com.consultafacil.domain.enums.ExamRequestStatus;
import com.consultafacil.domain.enums.ExamSchedulingStatus;
import com.consultafacil.domain.port.out.ExamLabRepositoryPort;
import com.consultafacil.domain.port.out.ExamRequestRepositoryPort;
import com.consultafacil.domain.port.out.ExamSchedulingRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleExamService implements ScheduleExamUseCase {

    private final ExamRequestRepositoryPort examRequestRepository;
    private final ExamLabRepositoryPort examLabRepository;
    private final ExamSchedulingRepositoryPort examSchedulingRepository;

    @Transactional
    @Override
    public ExamSchedulingResponseDTO execute(String patientUserId, ScheduleExamDTO dto) {
        ExamRequest examRequest = examRequestRepository.findById(dto.getExamRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("ExamRequest", dto.getExamRequestId()));

        if (!examRequest.getPatient().getUser().getId().equals(patientUserId)) {
            throw new BadRequestException("Exam request does not belong to the authenticated patient");
        }

        if (examRequest.getStatus() == ExamRequestStatus.SCHEDULED) {
            throw new BadRequestException("Exam is already scheduled");
        }

        ExamLab examLab = examLabRepository.findById(dto.getExamLabId())
                .orElseThrow(() -> new ResourceNotFoundException("ExamLab", dto.getExamLabId()));

        boolean slotTaken = examSchedulingRepository
                .findByExamLabIdAndScheduledDate(dto.getExamLabId(), dto.getScheduledDate())
                .stream()
                .anyMatch(s -> s.getStatus() == ExamSchedulingStatus.SCHEDULED
                        && s.getScheduledTime().equals(dto.getScheduledTime()));

        if (slotTaken) {
            throw new BadRequestException("The selected time slot is no longer available");
        }

        ExamScheduling scheduling = examSchedulingRepository.save(ExamScheduling.builder()
                .examRequest(examRequest)
                .examLab(examLab)
                .scheduledDate(dto.getScheduledDate())
                .scheduledTime(dto.getScheduledTime())
                .notes(dto.getNotes())
                .build());

        examRequest.setStatus(ExamRequestStatus.SCHEDULED);
        examRequestRepository.save(examRequest);

        return toDTO(scheduling);
    }

    static ExamSchedulingResponseDTO toDTO(ExamScheduling s) {
        return ExamSchedulingResponseDTO.builder()
                .id(s.getId())
                .examRequestId(s.getExamRequest().getId())
                .examName(s.getExamRequest().getExamName())
                .examLabId(s.getExamLab().getId())
                .examLabName(s.getExamLab().getName())
                .examLabAddress(s.getExamLab().getAddress())
                .examLabCity(s.getExamLab().getCity())
                .examLabPhone(s.getExamLab().getPhone())
                .scheduledDate(s.getScheduledDate())
                .scheduledTime(s.getScheduledTime())
                .status(s.getStatus())
                .notes(s.getNotes())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
