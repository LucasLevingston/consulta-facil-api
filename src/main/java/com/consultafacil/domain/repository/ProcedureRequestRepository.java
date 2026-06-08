package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.ProcedureRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcedureRequestRepository extends JpaRepository<ProcedureRequest, String> {
    List<ProcedureRequest> findByPatientId(String patientId);
    List<ProcedureRequest> findByProfessionalId(String professionalId);
}
