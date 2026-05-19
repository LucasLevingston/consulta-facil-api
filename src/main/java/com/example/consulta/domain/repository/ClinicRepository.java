package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClinicRepository extends JpaRepository<Clinic, String> {

    List<Clinic> findByOwnerId(String ownerId);

    List<Clinic> findByStatus(String status);

    @Query(value = """
            SELECT * FROM clinics c
            WHERE c.status = 'ACTIVE'
              AND c.latitude IS NOT NULL
              AND c.longitude IS NOT NULL
              AND (6371 * acos(
                    cos(radians(:lat)) * cos(radians(c.latitude)) *
                    cos(radians(c.longitude) - radians(:lng)) +
                    sin(radians(:lat)) * sin(radians(c.latitude))
                  )) <= :radiusKm
            ORDER BY (6371 * acos(
                    cos(radians(:lat)) * cos(radians(c.latitude)) *
                    cos(radians(c.longitude) - radians(:lng)) +
                    sin(radians(:lat)) * sin(radians(c.latitude))
                  )) ASC
            """, nativeQuery = true)
    List<Clinic> findNearby(@Param("lat") double lat, @Param("lng") double lng, @Param("radiusKm") double radiusKm);
}
