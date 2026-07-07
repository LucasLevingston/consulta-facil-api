package com.consultafacil.adapter.out.persistence.patient;

import com.consultafacil.domain.entity.PatientVaccine;
import com.consultafacil.domain.port.out.patient.PatientVaccineRepositoryPort;
import com.consultafacil.domain.repository.patient.PatientVaccineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PatientVaccineRepositoryAdapter implements PatientVaccineRepositoryPort {

    private final PatientVaccineRepository patientVaccineRepository;

    @Override
    public PatientVaccine save(PatientVaccine vaccine) {
        return patientVaccineRepository.save(vaccine);
    }

    @Override
    public List<PatientVaccine> findByPatientProfileId(String patientProfileId) {
        return patientVaccineRepository.findByPatientProfileId(patientProfileId);
    }

    @Override
    public Optional<PatientVaccine> findById(String id) {
        return patientVaccineRepository.findById(id);
    }

    @Override
    public void delete(PatientVaccine vaccine) {
        patientVaccineRepository.delete(vaccine);
    }
}
