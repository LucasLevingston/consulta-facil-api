package com.consultafacil.domain.port.out.patient;

import com.consultafacil.domain.entity.PatientDocument;

import java.util.List;
import java.util.Optional;

public interface PatientDocumentRepositoryPort {

    PatientDocument save(PatientDocument document);

    List<PatientDocument> findByPatientProfileId(String patientProfileId);

    Optional<PatientDocument> findById(String id);

    void delete(PatientDocument document);
}
