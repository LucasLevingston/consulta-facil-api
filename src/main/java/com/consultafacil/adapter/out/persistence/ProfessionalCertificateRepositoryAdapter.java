package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.ProfessionalCertificate;
import com.consultafacil.domain.port.out.ProfessionalCertificateRepositoryPort;
import com.consultafacil.domain.repository.ProfessionalCertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProfessionalCertificateRepositoryAdapter implements ProfessionalCertificateRepositoryPort {

    private final ProfessionalCertificateRepository repository;

    @Override
    public ProfessionalCertificate save(ProfessionalCertificate certificate) {
        return repository.save(certificate);
    }

    @Override
    public List<ProfessionalCertificate> findByProfessionalProfileId(String professionalProfileId) {
        return repository.findByProfessionalProfileId(professionalProfileId);
    }

    @Override
    public Optional<ProfessionalCertificate> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public void delete(ProfessionalCertificate certificate) {
        repository.delete(certificate);
    }
}
