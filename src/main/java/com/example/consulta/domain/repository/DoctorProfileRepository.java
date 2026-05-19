package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.DoctorProfile;
import com.example.consulta.domain.enums.DoctorProfileStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, String> {
    Optional<DoctorProfile> findByUserId(String userId);
    Page<DoctorProfile> findByStatus(DoctorProfileStatus status, Pageable pageable);
    Page<DoctorProfile> findBySpecialtyContainingIgnoreCaseAndStatus(String specialty, DoctorProfileStatus status, Pageable pageable);
    boolean existsByLicenseNumber(String licenseNumber);

    @Query(value = """
            SELECT * FROM doctor_profiles d
            WHERE d.status = 'ACTIVE'
              AND d.latitude IS NOT NULL
              AND d.longitude IS NOT NULL
              AND (:specialty IS NULL OR LOWER(d.specialty) LIKE LOWER(CONCAT('%', :specialty, '%')))
              AND (6371 * acos(
                    LEAST(1.0, cos(radians(:lat)) * cos(radians(d.latitude)) *
                    cos(radians(d.longitude) - radians(:lng)) +
                    sin(radians(:lat)) * sin(radians(d.latitude)))
                  )) <= :radiusKm
            ORDER BY (6371 * acos(
                    LEAST(1.0, cos(radians(:lat)) * cos(radians(d.latitude)) *
                    cos(radians(d.longitude) - radians(:lng)) +
                    sin(radians(:lat)) * sin(radians(d.latitude)))
                  )) ASC
            """, nativeQuery = true)
    List<DoctorProfile> findNearby(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm,
            @Param("specialty") String specialty);
}
