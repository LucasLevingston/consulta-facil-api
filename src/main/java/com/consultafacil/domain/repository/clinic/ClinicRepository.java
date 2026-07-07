package com.consultafacil.domain.repository.clinic;

import com.consultafacil.domain.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClinicRepository extends JpaRepository<Clinic, String> {

    @Query("""
            SELECT DISTINCT c FROM Clinic c
            LEFT JOIN FETCH c.members m
            LEFT JOIN FETCH m.professionalProfile dp
            LEFT JOIN FETCH dp.user
            LEFT JOIN FETCH c.owner
            WHERE c.owner.id = :ownerId
            """)
    List<Clinic> findByOwnerId(@Param("ownerId") String ownerId);

    @Query("""
            SELECT DISTINCT c FROM Clinic c
            LEFT JOIN FETCH c.members m
            LEFT JOIN FETCH m.professionalProfile dp
            LEFT JOIN FETCH dp.user
            LEFT JOIN FETCH c.owner
            WHERE c.status = :status
            """)
    List<Clinic> findByStatus(@Param("status") String status);

    @Query("""
            SELECT DISTINCT c FROM Clinic c
            LEFT JOIN FETCH c.members m
            LEFT JOIN FETCH m.professionalProfile dp
            LEFT JOIN FETCH dp.user
            LEFT JOIN FETCH c.owner
            WHERE c.id = :id
            """)
    Optional<Clinic> findByIdWithMembers(@Param("id") String id);

    @Query(value = """
            SELECT * FROM clinics c
            WHERE c.status = 'ACTIVE'
              AND c.latitude IS NOT NULL
              AND c.longitude IS NOT NULL
              AND (6371 * acos(
                    LEAST(1.0, cos(radians(:lat)) * cos(radians(c.latitude)) *
                    cos(radians(c.longitude) - radians(:lng)) +
                    sin(radians(:lat)) * sin(radians(c.latitude)))
                  )) <= :radiusKm
            ORDER BY (6371 * acos(
                    LEAST(1.0, cos(radians(:lat)) * cos(radians(c.latitude)) *
                    cos(radians(c.longitude) - radians(:lng)) +
                    sin(radians(:lat)) * sin(radians(c.latitude)))
                  )) ASC
            """, nativeQuery = true)
    List<Clinic> findNearby(@Param("lat") double lat, @Param("lng") double lng, @Param("radiusKm") double radiusKm);
}
