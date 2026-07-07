package com.consultafacil.domain.repository.examlab;

import com.consultafacil.domain.entity.ExamLab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamLabRepository extends JpaRepository<ExamLab, String> {

    List<ExamLab> findByStatus(String status);

    @Query(value = """
            SELECT * FROM exam_labs e
            WHERE e.status = 'ACTIVE'
              AND e.latitude IS NOT NULL
              AND e.longitude IS NOT NULL
              AND (6371 * acos(
                    LEAST(1.0, cos(radians(:lat)) * cos(radians(e.latitude)) *
                    cos(radians(e.longitude) - radians(:lng)) +
                    sin(radians(:lat)) * sin(radians(e.latitude)))
                  )) <= :radiusKm
            ORDER BY (6371 * acos(
                    LEAST(1.0, cos(radians(:lat)) * cos(radians(e.latitude)) *
                    cos(radians(e.longitude) - radians(:lng)) +
                    sin(radians(:lat)) * sin(radians(e.latitude)))
                  )) ASC
            """, nativeQuery = true)
    List<ExamLab> findNearby(@Param("lat") double lat, @Param("lng") double lng, @Param("radiusKm") double radiusKm);
}
