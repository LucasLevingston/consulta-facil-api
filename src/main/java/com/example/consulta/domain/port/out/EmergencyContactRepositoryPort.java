package com.example.consulta.domain.port.out;

import com.example.consulta.domain.entity.EmergencyContact;

import java.util.Optional;

public interface EmergencyContactRepositoryPort {

    EmergencyContact save(EmergencyContact contact);

    Optional<EmergencyContact> findByPatientProfileId(String patientProfileId);
}
