package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, String> {
    Optional<MedicalRecord> findByPatientProfileId(String patientProfileId);
}
