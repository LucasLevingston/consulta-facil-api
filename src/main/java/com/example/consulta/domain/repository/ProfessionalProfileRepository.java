package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.ProfessionalProfile;
import com.example.consulta.domain.enums.ProfessionalProfileStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessionalProfileRepository extends JpaRepository<ProfessionalProfile, String> {
    Optional<ProfessionalProfile> findByUserId(String userId);
    Page<ProfessionalProfile> findByStatus(ProfessionalProfileStatus status, Pageable pageable);
    Page<ProfessionalProfile> findBySpecialtyContainingIgnoreCaseAndStatus(String specialty, ProfessionalProfileStatus status, Pageable pageable);
    boolean existsByLicenseNumber(String licenseNumber);

    @Query("SELECT p FROM ProfessionalProfile p WHERE p.status = 'ACTIVE' " +
           "AND (:profession IS NULL OR LOWER(p.profession) LIKE LOWER(CONCAT('%', :profession, '%'))) " +
           "AND (:specialty IS NULL OR LOWER(p.specialty) LIKE LOWER(CONCAT('%', :specialty, '%'))) " +
           "AND (:name IS NULL OR LOWER(p.user.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<ProfessionalProfile> findActiveWithFilters(
            @Param("profession") String profession,
            @Param("specialty") String specialty,
            @Param("name") String name,
            Pageable pageable);

    @Query(value = """
            SELECT * FROM professional_profiles p
            WHERE p.status = 'ACTIVE'
              AND p.latitude IS NOT NULL
              AND p.longitude IS NOT NULL
              AND (:specialty IS NULL OR LOWER(p.specialty) LIKE LOWER(CONCAT('%', :specialty, '%')))
              AND (:profession IS NULL OR LOWER(p.profession) LIKE LOWER(CONCAT('%', :profession, '%')))
              AND (6371 * acos(
                    LEAST(1.0, cos(radians(:lat)) * cos(radians(p.latitude)) *
                    cos(radians(p.longitude) - radians(:lng)) +
                    sin(radians(:lat)) * sin(radians(p.latitude)))
                  )) <= :radiusKm
            ORDER BY (6371 * acos(
                    LEAST(1.0, cos(radians(:lat)) * cos(radians(p.latitude)) *
                    cos(radians(p.longitude) - radians(:lng)) +
                    sin(radians(:lat)) * sin(radians(p.latitude)))
                  )) ASC
            """, nativeQuery = true)
    List<ProfessionalProfile> findNearby(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm,
            @Param("specialty") String specialty,
            @Param("profession") String profession);
}
