package com.consultafacil.adapter.out.persistence.insurance;

import com.consultafacil.domain.entity.PatientInsurance;
import com.consultafacil.domain.port.out.insurance.PatientInsuranceRepositoryPort;
import com.consultafacil.domain.repository.insurance.PatientInsuranceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PatientInsuranceRepositoryAdapter implements PatientInsuranceRepositoryPort {

    private final PatientInsuranceRepository repository;

    @Override
    public PatientInsurance save(PatientInsurance entity) {
        return repository.save(entity);
    }

    @Override
    public List<PatientInsurance> findByPatientId(String patientId) {
        return repository.findByPatientId(patientId);
    }
}
