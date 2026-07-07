package com.consultafacil.domain.port.out.patient;

import com.consultafacil.domain.entity.PatientProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PatientProfileRepositoryPort {

    PatientProfile save(PatientProfile profile);

    Optional<PatientProfile> findById(String id);

    Optional<PatientProfile> findByUserId(String userId);

    Page<PatientProfile> findAll(Pageable pageable);
}
