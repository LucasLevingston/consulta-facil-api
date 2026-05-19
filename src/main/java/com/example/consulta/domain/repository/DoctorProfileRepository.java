package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.DoctorProfile;
import com.example.consulta.domain.enums.DoctorProfileStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, String> {
    Optional<DoctorProfile> findByUserId(String userId);
    Page<DoctorProfile> findByStatus(DoctorProfileStatus status, Pageable pageable);
    Page<DoctorProfile> findBySpecialtyContainingIgnoreCaseAndStatus(String specialty, DoctorProfileStatus status, Pageable pageable);
    boolean existsByLicenseNumber(String licenseNumber);
}
