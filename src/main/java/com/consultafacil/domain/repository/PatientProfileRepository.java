package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.PatientProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, String> {

    @EntityGraph(attributePaths = {"user"})
    @Override
    Page<PatientProfile> findAll(Pageable pageable);

    Optional<PatientProfile> findByUserId(String userId);
}
