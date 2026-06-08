package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.EmergencyContact;

import java.util.Optional;

public interface EmergencyContactRepositoryPort {

    EmergencyContact save(EmergencyContact contact);

    Optional<EmergencyContact> findByPatientProfileId(String patientProfileId);
}
