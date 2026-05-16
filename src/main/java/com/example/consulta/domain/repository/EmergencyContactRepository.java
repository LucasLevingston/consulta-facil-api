package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, String> {
    Optional<EmergencyContact> findByPatientProfileId(String patientProfileId);
}
