package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.ExamLab;

import java.util.List;
import java.util.Optional;

public interface ExamLabRepositoryPort {

    ExamLab save(ExamLab examLab);

    Optional<ExamLab> findById(String id);

    List<ExamLab> findByStatus(String status);

    List<ExamLab> findNearby(double lat, double lng, double radiusKm);

    void deleteById(String id);
}
