package com.consultafacil.application.service.examlab;

import com.consultafacil.api.dto.examlab.ExamLabResponseDTO;
import com.consultafacil.application.port.in.GetExamLabByIdUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.port.out.ExamLabRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetExamLabByIdService implements GetExamLabByIdUseCase {

    private final ExamLabRepositoryPort examLabRepository;

    @Transactional(readOnly = true)
    @Override
    public ExamLabResponseDTO execute(String id) {
        return examLabRepository.findById(id)
                .map(CreateExamLabService::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("ExamLab", id));
    }
}
