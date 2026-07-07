package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.ExamLab;
import com.consultafacil.domain.port.out.ExamLabRepositoryPort;
import com.consultafacil.domain.repository.ExamLabRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExamLabRepositoryAdapter implements ExamLabRepositoryPort {

    private final ExamLabRepository examLabRepository;

    @Override
    public ExamLab save(ExamLab examLab) {
        return examLabRepository.save(examLab);
    }

    @Override
    public Optional<ExamLab> findById(String id) {
        return examLabRepository.findById(id);
    }

    @Override
    public List<ExamLab> findByStatus(String status) {
        return examLabRepository.findByStatus(status);
    }

    @Override
    public List<ExamLab> findNearby(double lat, double lng, double radiusKm) {
        return examLabRepository.findNearby(lat, lng, radiusKm);
    }

    @Override
    public void deleteById(String id) {
        examLabRepository.deleteById(id);
    }
}
