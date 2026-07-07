package com.consultafacil.domain.port.out.dependent;

import com.consultafacil.domain.entity.Dependent;

import java.util.List;
import java.util.Optional;

public interface DependentRepositoryPort {
    Dependent save(Dependent dependent);
    List<Dependent> findByGuardianId(String guardianUserId);
    Optional<Dependent> findById(String id);
    void delete(Dependent dependent);
}
