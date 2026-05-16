package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, String> {
    Optional<MedicalRecord> findByPatientProfileId(String patientProfileId);
}
