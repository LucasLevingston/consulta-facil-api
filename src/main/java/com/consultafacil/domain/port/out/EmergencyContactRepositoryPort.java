package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.EmergencyContact;

import java.util.List;
import java.util.Optional;

public interface EmergencyContactRepositoryPort {

    EmergencyContact save(EmergencyContact contact);

    List<EmergencyContact> findByPatientProfileId(String patientProfileId);

    Optional<EmergencyContact> findById(String id);

    void delete(EmergencyContact contact);
}
