package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.Specialty;
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
    boolean existsByLicenseNumber(String licenseNumber);

    @Query("SELECT p FROM ProfessionalProfile p JOIN FETCH p.user u WHERE p.specialty = :specialty AND p.status = :status")
    Page<ProfessionalProfile> findBySpecialtyAndStatus(
            @Param("specialty") Specialty specialty,
            @Param("status") ProfessionalProfileStatus status,
            Pageable pageable);

    @Query(value = """
            SELECT p.* FROM professional_profiles p
            JOIN users u ON p.user_id = u.id
            WHERE p.status = 'ACTIVE'
              AND ('' = :profession OR p.profession = :profession)
              AND ('' = :specialty OR p.specialty = :specialty)
              AND ('' = :name OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """,
           countQuery = """
            SELECT COUNT(*) FROM professional_profiles p
            JOIN users u ON p.user_id = u.id
            WHERE p.status = 'ACTIVE'
              AND ('' = :profession OR p.profession = :profession)
              AND ('' = :specialty OR p.specialty = :specialty)
              AND ('' = :name OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """,
           nativeQuery = true)
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
              AND (:specialty = '' OR p.specialty = :specialty)
              AND (:profession = '' OR p.profession = :profession)
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
