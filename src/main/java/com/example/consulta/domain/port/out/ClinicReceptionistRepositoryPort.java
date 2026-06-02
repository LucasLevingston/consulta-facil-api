package com.example.consulta.domain.port.out;

import com.example.consulta.domain.entity.ClinicReceptionist;

import java.util.List;
import java.util.Optional;

public interface ClinicReceptionistRepositoryPort {

    ClinicReceptionist save(ClinicReceptionist receptionist);

    Optional<ClinicReceptionist> findById(String id);

    List<ClinicReceptionist> findByClinicId(String clinicId);

    Optional<ClinicReceptionist> findByClinicIdAndUserId(String clinicId, String userId);

    boolean existsByClinicIdAndUserId(String clinicId, String userId);

    boolean existsByUserId(String userId);

    void delete(ClinicReceptionist receptionist);
}
