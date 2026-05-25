package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.ProfessionalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfessionalServiceRepository extends JpaRepository<ProfessionalService, String> {
    List<ProfessionalService> findByProfessionalIdAndActiveTrue(String professionalId);
}
