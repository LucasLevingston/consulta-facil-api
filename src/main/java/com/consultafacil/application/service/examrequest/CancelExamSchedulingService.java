package com.consultafacil.application.service.examrequest;

import com.consultafacil.application.port.in.CancelExamSchedulingUseCase;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ExamScheduling;
import com.consultafacil.domain.enums.ExamRequestStatus;
import com.consultafacil.domain.enums.ExamSchedulingStatus;
import com.consultafacil.domain.port.out.ExamRequestRepositoryPort;
import com.consultafacil.domain.port.out.ExamSchedulingRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelExamSchedulingService implements CancelExamSchedulingUseCase {

    private final ExamSchedulingRepositoryPort examSchedulingRepository;
    private final ExamRequestRepositoryPort examRequestRepository;

    @Transactional
    @Override
    public void execute(String schedulingId, String userId) {
        ExamScheduling scheduling = examSchedulingRepository.findById(schedulingId)
                .orElseThrow(() -> new ResourceNotFoundException("ExamScheduling", schedulingId));

        String patientUserId = scheduling.getExamRequest().getPatient().getUser().getId();
        if (!patientUserId.equals(userId)) {
            throw new BadRequestException("Scheduling does not belong to the authenticated user");
        }

        if (scheduling.getStatus() == ExamSchedulingStatus.CANCELLED) {
            throw new BadRequestException("Scheduling is already cancelled");
        }

        scheduling.setStatus(ExamSchedulingStatus.CANCELLED);
        examSchedulingRepository.save(scheduling);

        var examRequest = scheduling.getExamRequest();
        examRequest.setStatus(ExamRequestStatus.PENDING);
        examRequestRepository.save(examRequest);
    }
}
