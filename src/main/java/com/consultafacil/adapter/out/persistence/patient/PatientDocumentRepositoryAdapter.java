package com.consultafacil.adapter.out.persistence.patient;

import com.consultafacil.domain.entity.PatientDocument;
import com.consultafacil.domain.port.out.patient.PatientDocumentRepositoryPort;
import com.consultafacil.domain.repository.patient.PatientDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PatientDocumentRepositoryAdapter implements PatientDocumentRepositoryPort {

    private final PatientDocumentRepository patientDocumentRepository;

    @Override
    public PatientDocument save(PatientDocument document) {
        return patientDocumentRepository.save(document);
    }

    @Override
    public List<PatientDocument> findByPatientProfileId(String patientProfileId) {
        return patientDocumentRepository.findByPatientProfileId(patientProfileId);
    }

    @Override
    public Optional<PatientDocument> findById(String id) {
        return patientDocumentRepository.findById(id);
    }

    @Override
    public void delete(PatientDocument document) {
        patientDocumentRepository.delete(document);
    }
}
