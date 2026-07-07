package com.consultafacil.domain.repository.professional.enrichment;

import com.consultafacil.domain.entity.ProfessionalCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfessionalCertificateRepository extends JpaRepository<ProfessionalCertificate, String> {
    List<ProfessionalCertificate> findByProfessionalProfileId(String professionalProfileId);
}
