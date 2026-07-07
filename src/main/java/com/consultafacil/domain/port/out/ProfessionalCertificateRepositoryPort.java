package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.ProfessionalCertificate;

import java.util.List;
import java.util.Optional;

public interface ProfessionalCertificateRepositoryPort {
    ProfessionalCertificate save(ProfessionalCertificate certificate);
    List<ProfessionalCertificate> findByProfessionalProfileId(String professionalProfileId);
    Optional<ProfessionalCertificate> findById(String id);
    void delete(ProfessionalCertificate certificate);
}
