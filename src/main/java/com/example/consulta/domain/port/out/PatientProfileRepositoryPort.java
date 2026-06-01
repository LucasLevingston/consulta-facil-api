package com.example.consulta.domain.port.out;

import com.example.consulta.domain.entity.PatientProfile;

import java.util.Optional;

public interface PatientProfileRepositoryPort {

    PatientProfile save(PatientProfile profile);

    Optional<PatientProfile> findByUserId(String userId);
}
