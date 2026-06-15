package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.PatientVaccine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientVaccineRepository extends JpaRepository<PatientVaccine, String> {
    List<PatientVaccine> findByPatientProfileId(String patientProfileId);
}
