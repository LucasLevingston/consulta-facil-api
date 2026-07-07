package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.PatientDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientDocumentRepository extends JpaRepository<PatientDocument, String> {
    List<PatientDocument> findByPatientProfileId(String patientProfileId);
}
