package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.Dependent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DependentRepository extends JpaRepository<Dependent, String> {
    List<Dependent> findByGuardianId(String guardianUserId);
}
