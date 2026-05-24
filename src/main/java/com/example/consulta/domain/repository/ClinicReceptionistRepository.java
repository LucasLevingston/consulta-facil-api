package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.ClinicReceptionist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicReceptionistRepository extends JpaRepository<ClinicReceptionist, String> {

    List<ClinicReceptionist> findByClinicId(String clinicId);

    Optional<ClinicReceptionist> findByClinicIdAndUserId(String clinicId, String userId);

    boolean existsByClinicIdAndUserId(String clinicId, String userId);

    boolean existsByUserId(String userId);
}
