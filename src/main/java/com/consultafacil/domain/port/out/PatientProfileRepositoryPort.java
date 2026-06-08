package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.PatientProfile;

import java.util.Optional;

public interface PatientProfileRepositoryPort {

    PatientProfile save(PatientProfile profile);

    Optional<PatientProfile> findById(String id);

    Optional<PatientProfile> findByUserId(String userId);
}
