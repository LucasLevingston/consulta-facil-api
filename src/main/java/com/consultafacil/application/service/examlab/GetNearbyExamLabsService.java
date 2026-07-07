package com.consultafacil.application.service.examlab;

import com.consultafacil.api.dto.examlab.ExamLabResponseDTO;
import com.consultafacil.application.port.in.GetNearbyExamLabsUseCase;
import com.consultafacil.domain.port.out.ExamLabRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetNearbyExamLabsService implements GetNearbyExamLabsUseCase {

    private final ExamLabRepositoryPort examLabRepository;

    @Transactional(readOnly = true)
    @Override
    public List<ExamLabResponseDTO> execute(double lat, double lng, double radiusKm) {
        return examLabRepository.findNearby(lat, lng, radiusKm).stream()
                .map(CreateExamLabService::toDTO)
                .toList();
    }
}
