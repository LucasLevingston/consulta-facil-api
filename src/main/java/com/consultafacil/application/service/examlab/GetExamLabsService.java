package com.consultafacil.application.service.examlab;

import com.consultafacil.api.dto.examlab.ExamLabResponseDTO;
import com.consultafacil.application.port.in.examlab.GetExamLabsUseCase;
import com.consultafacil.domain.port.out.examlab.ExamLabRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetExamLabsService implements GetExamLabsUseCase {

    private final ExamLabRepositoryPort examLabRepository;

    @Transactional(readOnly = true)
    @Override
    public List<ExamLabResponseDTO> execute() {
        return examLabRepository.findByStatus("ACTIVE").stream()
                .map(CreateExamLabService::toDTO)
                .toList();
    }
}
