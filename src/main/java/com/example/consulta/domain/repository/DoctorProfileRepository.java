package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.DoctorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, String> {
    Optional<DoctorProfile> findByUserId(String userId);
    Page<DoctorProfile> findBySpecialtyContainingIgnoreCase(String specialty, Pageable pageable);
    boolean existsByLicenseNumber(String licenseNumber);
}
