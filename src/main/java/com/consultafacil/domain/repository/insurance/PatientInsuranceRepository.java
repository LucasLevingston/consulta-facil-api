package com.consultafacil.domain.repository.insurance;

import com.consultafacil.domain.entity.PatientInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientInsuranceRepository extends JpaRepository<PatientInsurance, String> {

    List<PatientInsurance> findByPatientId(String patientId);
}
